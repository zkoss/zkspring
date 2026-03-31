package org.zkoss.zkspringessentials.bigbank.web;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zkspringessentials.bigbank.Account;
import org.zkoss.zkspringessentials.bigbank.BankService;
import org.zkoss.zul.ListModelList;

/**
 * MVVM ViewModel demonstrating {@code @PreAuthorize} with AspectJ Compile-Time Weaving (CTW).
 *
 * <p>{@code @PreAuthorize} is placed directly on the command method.  The
 * {@code spring-security-aspects} library weaves the security check into the bytecode
 * at build time — no CGLIB proxy is generated, so {@code @BindingParam} parameters
 * arrive with their correct values.
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
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'TELLER')")
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
