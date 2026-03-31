package org.zkoss.zkspringessentials.bigbank.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkspringessentials.app.security.RequiresRole;
import org.zkoss.zkspringessentials.bigbank.Account;
import org.zkoss.zkspringessentials.bigbank.BankService;
import org.zkoss.zul.Button;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Window;

/**
 * MVC Composer demonstrating Spring AOP authorization with {@link RequiresRole}.
 *
 * <p>Unlike the MVVM ViewModel approach, the event handler receives a {@link MouseEvent}
 * parameter — not {@code @BindingParam}.  CGLIB-proxying a Composer is safe because
 * CGLIB copies method-level annotations (including {@code @Listen} and {@code @RequiresRole})
 * to proxy overrides, and there are no parameter annotations to lose.
 *
 * <p>{@link org.zkoss.zkspringessentials.app.security.ZkAuthorizationAspect} therefore
 * handles authorization at runtime via a CGLIB proxy — no AspectJ CTW required.
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
    @RequiresRole({"ROLE_SUPERVISOR", "ROLE_TELLER"})
    public void onAdjustBalance(MouseEvent event) {
        Button btn = (Button) event.getTarget();
        long accountId = ((Number) btn.getAttribute("accountId")).longValue();
        double amount = Double.parseDouble(btn.getAttribute("amount").toString());

        Account account = bankService.readAccount(accountId);
        account.setBalance(bankService.post(account, amount).getBalance());

        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getId() == accountId) {
                accounts.set(i, account);
                break;
            }
        }
    }
}
