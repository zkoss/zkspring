package org.zkoss.zkspringessentials.bigbank.web;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zkspringessentials.bigbank.Account;
import org.zkoss.zkspringessentials.bigbank.BankService;
import org.zkoss.zkspringessentials.bigbank.security.*;
import org.zkoss.zul.ListModelList;

/**
 * MVVM ViewModel demonstrating PhaseListener-based centralized authorization.
 *
 * <p>There is deliberately no {@code @PreAuthorize} here.  Authorization is enforced by
 * {@link AuthPhaseListener}, which intercepts every {@code @Command} before execution and
 * checks the rule registered in {@link CommandAuthorizationRules} for this ViewModel class.
 */
@Component
@Scope("prototype")
public class BigbankViewModel3 {

    @Autowired
    private BankService bankService;

    private ListModelList<Account> accounts;

    @PostConstruct
    public void init() {
        accounts = new ListModelList<>(bankService.findAccounts());
    }

    @Command
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
