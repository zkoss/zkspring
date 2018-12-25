package bigbank.web;

import bigbank.Account;
import bigbank.BankServiceScenario1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.ListModelList;

@VariableResolver(DelegatingVariableResolver.class)
public class BigbankViewModel {

	@WireVariable
	private BankServiceScenario1 bankServiceScenario1;

	private ListModelList<Account> accounts;

	@Init
	public void init() {
		accounts = new ListModelList<Account>(bankServiceScenario1.findAccounts());
	}

	@Command
	public void adjustBalance(@BindingParam("accountId") Long id, @BindingParam("amount") Double amount) {
		final Account account = bankServiceScenario1.readAccount(id);
		final Account updatedAccount = bankServiceScenario1.post(account, amount);
		account.setBalance(updatedAccount.getBalance());
		BindUtils.postNotifyChange(null, null, account, "balance");
	}

	public ListModelList<Account> getAccounts() {
		return accounts;
	}
}
