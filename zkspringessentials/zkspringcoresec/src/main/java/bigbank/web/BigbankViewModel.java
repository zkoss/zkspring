package bigbank.web;

import bigbank.Account;
import bigbank.BankServiceScenario;
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
	private BankServiceScenario bankServiceScenario;

	private ListModelList<Account> accounts;

	@Init
	public void init() {
		accounts = new ListModelList<Account>(bankServiceScenario.findAccounts());
	}

	@Command
	public void adjustBalance(@BindingParam("accountId") Long id, @BindingParam("amount") Double amount) {
		final Account account = bankServiceScenario.readAccount(id);
		account.setBalance(bankServiceScenario.post(account, amount).getBalance());
		BindUtils.postNotifyChange(null, null, account, "balance");
	}

	public ListModelList<Account> getAccounts() {
		return accounts;
	}
}
