package com.ettrema.pay;

/**
 *
 */
public class Transaction {
    public final Amount amount;
    public final String merchantRef;
    public final String transactionRef;

    public Transaction(Amount amount, String merchantRef, String transactionRef) {
        this.amount = amount;
        this.merchantRef = merchantRef;
        this.transactionRef = transactionRef;
    }

    
}
