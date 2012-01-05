package com.ettrema.pay.paypal;

/**
 * Interface to report if a given transaction id has been used before.
 *
 * @author brad
 */
public interface TransactionIdChecker {
    /**
     * Return true if the given transaction id has been previously used.
     *
     * Note that an IPN notification reporting a Pending transaction should not
     * be considered to have 'used' the id. Its only used when we see a Completed
     * notification.
     *
     * @param txId
     * @return
     */
    boolean hasBeenUsed(String txId);
}
