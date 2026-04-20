package org.zkoss.zkspringessentials.bigbank.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkspringessentials.bigbank.Account;
import org.zkoss.zkspringessentials.bigbank.BankService;
import org.zkoss.zkspringessentials.bigbank.security.ComposerAuthorizationAspect;
import org.zkoss.zul.Button;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.Window;

/**
 * MVC Composer demonstrating centralized authorization via AspectJ pointcut rules.
 *
 * <p>There is deliberately no {@code @PreAuthorize} here. Authorization is enforced by
 * {@link ComposerAuthorizationAspect}, which declares pointcut rules at the class level —
 * the ZK equivalent of Spring Security's {@code requestMatchers("/admin/**")}.
 */
@Component
@Scope("prototype")
public class BigbankComposer2 extends SelectorComposer<Window> {

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
    public void onAdjustBalance(MouseEvent event) {
        Button btn = (Button) event.getTarget();
        long accountId = ((Number) btn.getAttribute("accountId")).longValue();
        double amount = Double.parseDouble(btn.getAttribute("amount").toString());

        Account account = bankService.readAccount(accountId);
        account.setBalance(bankService.post(account, amount).getBalance());

        Row row = (Row) btn.getParent();
        ((Label) row.getChildren().get(2)).setValue(String.valueOf(account.getBalance()));
    }
}
