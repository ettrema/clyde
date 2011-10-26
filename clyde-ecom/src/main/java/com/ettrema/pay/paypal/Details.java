package com.ettrema.pay.paypal;

import java.math.BigDecimal;

public class Details {

    private static final String STATUS_COMPLETED = "Completed";
    private static final String STATUS_PENDING = "Pending";

    public final String itemName;
    public final String itemNumber;
    public final String paymentStatus;
    public final String paymentAmount;
    public final String paymentCurrency;
    public final String txnId;
    public final String receiverEmail;
    public final String payerEmail;

    public Details( String itemName, String itemNumber, String paymentStatus, String paymentAmount, String paymentCurrency, String txnId, String receiverEmail, String payerEmail ) {
        super();
        this.itemName = itemName;
        this.itemNumber = itemNumber;
        this.paymentStatus = paymentStatus;
        this.paymentAmount = paymentAmount;
        this.paymentCurrency = paymentCurrency;
        this.txnId = txnId;
        this.receiverEmail = receiverEmail;
        this.payerEmail = payerEmail;
    }

    public BigDecimal amount() throws Exception {
        if( paymentAmount == null || paymentAmount.length() == 0 ) return null;
        try {
            return new BigDecimal( paymentAmount );
        } catch( NumberFormatException e ) {
            throw new Exception( "Invalid amount: " + paymentAmount );
        }
    }

    @Override
    public String toString() {
        return "itemName:" + itemName + " - itemNumber:" + itemNumber + " - paymentStatus:" + paymentStatus + " - paymentAmount:" + paymentAmount + " - currency:" + paymentCurrency + " - txnId:" + txnId + " - receiverEmail:" + receiverEmail + " - payerEmail:" + payerEmail;
    }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equals( paymentStatus);
    }

    public boolean isPending() {
        return STATUS_PENDING.equals( paymentStatus);
    }

}
