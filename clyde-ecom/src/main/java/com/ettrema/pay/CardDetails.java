package com.ettrema.pay;

/**
 *
 */
public class CardDetails {
    public final CardNumber cardNumber;
    public final Expiry expiry;
    public final String postcode;
    public final String address;
    public final String cardHolder;
    public final String cardVerificationNo;

    public CardDetails(CardNumber cardNumber, Expiry expiry, String postcode, String address, String cardHolder,String cardVerificationNo) {
        this.cardNumber = cardNumber;
        this.expiry = expiry;
        this.postcode = postcode;
        this.address = address;
        this.cardHolder = cardHolder;
        this.cardVerificationNo = cardVerificationNo;
    }

}
