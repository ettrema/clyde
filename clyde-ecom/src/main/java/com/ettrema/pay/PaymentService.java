
package com.ettrema.pay;

public interface PaymentService {
    /**
     * Make a payment with a new customer
     *
     * @param amount
     * @param details - all the details for the payment
     * @return
     */
    Result doPayment(Transaction transaction, CardDetails details);

    /**
     * Make a payment, and enable rebilling with the given id
     *
     * @param amount
     * @param details
     * @param billingId
     * @return
     */
    Result doPayment(Transaction transaction, String billingIdToSet, CardDetails details);

    /**
     * Make a payment using customer card details stored
     *
     * @param amount
     * @param details
     * @return
     */
    Result rebill(Transaction transaction, String registeredBillingId, String cardVerificationNo);
    
    /**
     * Update the credit card details stored against the given billingid
     * 
     * @param details
     * @param billingId
     */
    void update(CardDetails details, String billingId);

    void refund(String txRef, Amount amount);
}
