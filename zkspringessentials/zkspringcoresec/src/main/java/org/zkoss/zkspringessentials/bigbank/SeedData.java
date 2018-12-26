package org.zkoss.zkspringessentials.bigbank;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component("seedData")
public class SeedData implements InitializingBean {
	private BankDao bankDao;

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(bankDao, "missing bankDao");
		bankDao.createOrUpdateAccount(new Account("rod"));
		bankDao.createOrUpdateAccount(new Account("dianne"));
		bankDao.createOrUpdateAccount(new Account("scott"));
		bankDao.createOrUpdateAccount(new Account("peter"));
	}

	@Autowired
	public void setBankDao(BankDao bankDao) {
		this.bankDao = bankDao;
	}
	
}
