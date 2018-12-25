package bigbank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service("bankServiceScenario")
public class BankServiceScenarioImpl implements BankServiceScenario {
    private BankDao bankDao;

    @Autowired
    public BankServiceScenarioImpl(BankDao bankDao) {
        Assert.notNull(bankDao, "missing bankDao");
        this.bankDao = bankDao;
    }

    public Account[] findAccounts() {
        return this.bankDao.findAccounts();
    }

    public Account post(Account account, double amount) {
        Assert.notNull(account, "account missing");

        // We read account bank from DAO so it reflects the latest balance
        Account a = bankDao.readAccount(account.getId());
        if (account == null) {
            throw new IllegalArgumentException("Couldn't find requested account");
        }

        a.setBalance(a.getBalance() + amount);
        bankDao.createOrUpdateAccount(a);
        return a;
    }

    public Account readAccount(Long id) {
        return bankDao.readAccount(id);
    }
}
