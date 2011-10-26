package com.ettrema.pay.paypal;

/**
 *
 * @author brad
 */
public class InvalidNotificationException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidNotificationException(String reason) {
        super(reason);
    }

}
