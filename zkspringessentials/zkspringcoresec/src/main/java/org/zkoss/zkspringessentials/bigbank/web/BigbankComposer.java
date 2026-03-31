package org.zkoss.zkspringessentials.bigbank.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.springframework.security.access.prepost.PreAuthorize;
import org.zkoss.zkspringessentials.bigbank.Account;
import org.zkoss.zkspringessentials.bigbank.BankService;
import org.zkoss.zul.Button;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.Window;

/**
 * MVC Composer demonstrating {@code @PreAuthorize} with AspectJ Compile-Time Weaving (CTW).
 *
 * <p>{@code @PreAuthorize} is woven into the bytecode at build time by
 * {@code spring-security-aspects}, so no CGLIB proxy is involved.
 */
@Component
@Scope("prototype")
public class BigbankComposer extends SelectorComposer<Window> {

    @Autowired
    private BankService bankService;

    @Wire
    private Grid accountGrid;

    private ListModelList<Account> accounts;

    @Override
    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);
        accounts = new ListModelList<>(bankService.findAccounts());
        accountGrid.setModel(accounts);
    }

    @Listen("onClick = #accountGrid row button")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'TELLER')")
    public void onAdjustBalance(MouseEvent event) {
        Button btn = (Button) event.getTarget();
        long accountId = ((Number) btn.getAttribute("accountId")).longValue();
        double amount = Double.parseDouble(btn.getAttribute("amount").toString());

        Account account = bankService.readAccount(accountId);
        account.setBalance(bankService.post(account, amount).getBalance());

        // Update the balance label directly instead of replacing the model item,
        // because accounts.set() re-renders the row, creating new buttons without @Listen bindings.
        Row row = (Row) btn.getParent();
        ((Label) row.getChildren().get(2)).setValue(String.valueOf(account.getBalance()));
    }
}
