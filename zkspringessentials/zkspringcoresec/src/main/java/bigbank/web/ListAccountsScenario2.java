package bigbank.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import bigbank.BankServiceScenario1;

public class ListAccountsScenario2 implements Controller {

	private BankServiceScenario1 bankService;
	
	public ListAccountsScenario2(BankServiceScenario1 bankService) {
		Assert.notNull(bankService);
		this.bankService = bankService;
	}

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// Actual business logic
		ModelAndView mav = new ModelAndView("listAccounts2");
		mav.addObject("accounts", bankService.findAccounts());
		return mav;
	}
}
