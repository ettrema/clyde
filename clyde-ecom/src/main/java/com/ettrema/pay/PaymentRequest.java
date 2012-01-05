
package com.ettrema.pay;

import java.math.BigDecimal;

public class PaymentRequest {
    public final CardNumber cardNumber;
    public final Expiry expiry;
    public final String address;
    public final String cardHolder;
    public final BigDecimal amount;

    public PaymentRequest(CardNumber cardNumber, Expiry expiry, String address, String cardHolder, BigDecimal amount) {
        this.cardNumber = cardNumber;
        this.expiry = expiry;
        this.address = address;
        this.cardHolder = cardHolder;
        this.amount = amount;
    }
    
    
}
