package com.bradmcevoy.pay.paypal;

import java.math.BigDecimal;
import java.util.Map;

/**
 *
 * @author brad
 */
public interface InstantPaymentNotificationProcessor {

    /**
     * Validate the given set of parameters provided by an IPN call to our server.
     * 
     * 
     * 
     * 
     * @param params - map of parameters for the IPN request
     * @param amountDue - the expected amount
     * @param currency - the expected currency
     * @param onSuccess - runner to execute for valid completed notifications
     * @param onFailure - runner to execute for invalid notifications
     * @param onPending - runner for valid, but pending, notifications
     * @param idChecker - check that the txnId hasnt been used before
     * @return - value object containing validated information from paypal
     * @throws - InvalidNotificationException - if the transaction is not valid. Thrown
     * AFTER calling the failure runner
     */
    Details process (
            Map<String, String> params,
            BigDecimal amountDue,
            String currency,
            PostPaymentRunner onSuccess,
            PostPaymentRunner onFailure,
            PostPaymentRunner onPending,
            TransactionIdChecker idChecker ) throws InvalidNotificationException;
}
