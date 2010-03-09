package com.bradmcevoy.pay.paypal;

/**
 *
 * @author brad
 */
public class InvalidNotificationException extends Exception {

    public InvalidNotificationException(String reason) {
        super(reason);
    }

}
