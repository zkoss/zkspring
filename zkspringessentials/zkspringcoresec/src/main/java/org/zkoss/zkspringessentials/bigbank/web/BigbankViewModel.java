package org.zkoss.zkspringessentials.bigbank.web;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.*;
import org.zkoss.zkspringessentials.bigbank.*;
import org.zkoss.zul.ListModelList;

@Component
@Scope("prototype")
public class BigbankViewModel {

	@Autowired
	private BankService bankService;

	@Autowired
	private BigbankSecurityService securityService;

	private ListModelList<Account> accounts;

	@PostConstruct
	public void init() {
		accounts = new ListModelList<Account>(bankService.findAccounts());
	}

	@Command
	public void adjustBalance(@BindingParam("accountId") Long id, @BindingParam("amount") Double amount) {
		securityService.assertCanAdjustBalance();
		final Account account = bankService.readAccount(id);
		account.setBalance(bankService.post(account, amount).getBalance());
		BindUtils.postNotifyChange(null, null, account, "balance");
	}

	public ListModelList<Account> getAccounts() {
		return accounts;
	}
}
