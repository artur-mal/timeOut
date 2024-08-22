package org.example.transactiontimeouttest;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    @Resource(lookup = "jdbc/banking")
    private DataSource dataSource;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void deposit(Long accountId, double amount) {

        logger.log(Level.INFO, "Entering Deposit Method");
        LocalDateTime startTime = LocalDateTime.now();
        logger.log(Level.INFO, "Attempting to acquire lock on account with ID {0} at {1}", new Object[]{accountId, startTime.format(formatter)});

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        LocalDateTime lockAcquiredTime = null;

        try {
            // Obtain a connection from the data source
            connection = dataSource.getConnection();

            // Perform database operations using the connection
            String sql = "SELECT balance FROM accounts WHERE id = ? FOR UPDATE";

            ps = connection.prepareStatement(sql);
            ps.setLong(1, accountId);
            rs = ps.executeQuery();

            if (rs.next()) {
                lockAcquiredTime = LocalDateTime.now();
                logger.log(Level.INFO, "Lock acquired on account with ID {0} at {1}", new Object[]{accountId, lockAcquiredTime.format(formatter)});

                double currentBalance = rs.getDouble("balance");
                double newBalance = currentBalance + amount;

                // Artificial delay to simulate lock/connection being held for extra time
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.log(Level.SEVERE, "Thread was interrupted", e);
                }

                String updateSql = "UPDATE accounts SET balance = ? WHERE id = ?";
                try (PreparedStatement updatePs = connection.prepareStatement(updateSql)) {
                    updatePs.setDouble(1, newBalance);
                    updatePs.setLong(2, accountId);
                    updatePs.executeUpdate();
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database connection error", e);
        } finally {
            // Close the ResultSet, PreparedStatement, and Connection
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error closing ResultSet", e);
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error closing PreparedStatement", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error closing Connection", e);
                }
            }
        }

        LocalDateTime endTime = LocalDateTime.now();
        logger.log(Level.INFO, "Lock released on account with ID {0} at {1}", new Object[]{accountId, endTime.format(formatter)});

        if (lockAcquiredTime != null) {
            Duration lockDuration = Duration.between(lockAcquiredTime, endTime);
            logger.log(Level.INFO, "Lock held on account with ID {0} for {1} seconds", new Object[]{accountId, lockDuration.getSeconds()});
        }
    }

}