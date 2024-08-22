package org.example.transactiontimeouttest;

import javax.persistence.*;

@Entity
@Table(name = "accounts")
//@NamedQuery(name = "Account.findById", query = "SELECT a FROM Account a WHERE a.id = :id", lockMode = LockModeType.PESSIMISTIC_WRITE)
public class Account {
    @Id
    private Long id;
    private double balance;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}