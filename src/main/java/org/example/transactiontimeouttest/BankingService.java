package org.example.transactiontimeouttest;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class BankingService {

    Logger logger = Logger.getLogger(BankingService.class.getName());

    @PersistenceContext
    private EntityManager em;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void deposit(Long accountId, double amount) {

        logger.log(Level.INFO, "Entering Deposit Method");
        LocalDateTime startTime = LocalDateTime.now();
        logger.log(Level.INFO, "Attempting to acquire lock on account with ID {0} at {1}", new Object[]{accountId, startTime.format(formatter)});


        //Account account = em.find(Account.class, accountId, LockModeType.PESSIMISTIC_WRITE);
        Account account = em.find(Account.class, accountId, LockModeType.NONE);

        LocalDateTime lockAcquiredTime = LocalDateTime.now();
        logger.log(Level.INFO, "Lock acquired on account with ID {0} at {1}", new Object[]{accountId, lockAcquiredTime.format(formatter)});


        // Artificial delay to simulate lock being held for 20 seconds
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "Thread was interrupted", e);
        }


        account.setBalance(account.getBalance() + amount);
        em.merge(account);

        LocalDateTime endTime = LocalDateTime.now();
        logger.log(Level.INFO, "Lock released on account with ID {0} at {1}", new Object[]{accountId, endTime.format(formatter)});

        Duration lockDuration = Duration.between(lockAcquiredTime, endTime);
        logger.log(Level.INFO, "Lock held on account with ID {0} for {1} seconds", new Object[]{accountId, lockDuration.getSeconds()});
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void withdraw(Long accountId, double amount) {
        Account account = em.find(Account.class, accountId, LockModeType.PESSIMISTIC_WRITE);
        account.setBalance(account.getBalance() - amount);
        em.merge(account);
    }

    public List<Account> getAllAccounts() {
        TypedQuery<Account> query = em.createQuery("SELECT a FROM Account a", Account.class);
        return query.getResultList();
    }
}