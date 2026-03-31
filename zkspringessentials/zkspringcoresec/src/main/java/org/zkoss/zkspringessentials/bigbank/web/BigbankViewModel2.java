package org.zkoss.zkspringessentials.bigbank.web;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zkspringessentials.app.security.RequiresRole;
import org.zkoss.zkspringessentials.bigbank.Account;
import org.zkoss.zkspringessentials.bigbank.BankService;
import org.zkoss.zul.ListModelList;

/**
 * MVVM ViewModel demonstrating AspectJ Compile-Time Weaving (CTW) authorization.
 *
 * <p>{@code @RequiresRole} is placed directly on the command method.  Unlike the
 * Spring AOP approach, CTW weaves the security check into the bytecode at build time —
 * no CGLIB proxy is generated, so {@code @BindingParam} parameters arrive with their
 * correct values.
 *
 * <p>No {@link BigbankSecurityService} dependency is needed here; the security check
 * is handled transparently by {@link org.zkoss.zkspringessentials.app.security.CtwAuthorizationAspect}.
 */
@Component
@Scope("prototype")
public class BigbankViewModel2 {

    @Autowired
    private BankService bankService;

    private ListModelList<Account> accounts;

    @PostConstruct
    public void init() {
        accounts = new ListModelList<>(bankService.findAccounts());
    }

    @Command
    @RequiresRole({"ROLE_SUPERVISOR", "ROLE_TELLER"})
    public void adjustBalance(@BindingParam("accountId") Long id,
                              @BindingParam("amount") Double amount) {
        final Account account = bankService.readAccount(id);
        account.setBalance(bankService.post(account, amount).getBalance());
        BindUtils.postNotifyChange(null, null, account, "balance");
    }

    public ListModelList<Account> getAccounts() {
        return accounts;
    }
}
