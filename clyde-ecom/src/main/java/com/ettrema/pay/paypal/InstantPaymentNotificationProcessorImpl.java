package com.ettrema.pay.paypal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;

/**
 *
 * @author brad
 */
public class InstantPaymentNotificationProcessorImpl implements InstantPaymentNotificationProcessor {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( InstantPaymentNotificationProcessorImpl.class );
    private static final long serialVersionUID = 1L;
    private final String paypalUrl;
    private final String receiverEmail;

    public InstantPaymentNotificationProcessorImpl( String paypalUrl, String receiverEmail ) {
        this.paypalUrl = paypalUrl;
        this.receiverEmail = receiverEmail;
        log.debug( "configured on url: " + paypalUrl );
    }

    public Details process( Map<String, String> parameters, BigDecimal expectedAmount, String expectedCurrency, PostPaymentRunner onSuccess, PostPaymentRunner onFailure, PostPaymentRunner onPending, TransactionIdChecker idChecker ) throws InvalidNotificationException {
        log.debug( "process IPN: " + paypalUrl );
        try {
            String str = "cmd=_notify-validate";
            for( Map.Entry<String, String> entry : parameters.entrySet() ) {
                String paramName = entry.getKey();
                String paramValue = entry.getValue();
                str = str + "&" + paramName + "=" + URLEncoder.encode( paramValue, "ISO-8859-1" );
            }
            String url = paypalUrl + "?" + str;
            URL u = new URL( url );
            URLConnection uc = u.openConnection();
            uc.setDoOutput( true );
            uc.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
            PrintWriter pw = new PrintWriter( uc.getOutputStream() );
            pw.println( str );
            pw.close();
            BufferedReader in = new BufferedReader( new InputStreamReader( uc.getInputStream() ) );
            String res = in.readLine();
            in.close();

            Details details = new Details(
                    parameters.get( "item_name" ),
                    parameters.get( "item_name" ),
                    parameters.get( "payment_status" ),
                    parameters.get( "mc_gross" ),
                    parameters.get( "mc_currency" ),
                    parameters.get( "txn_id" ),
                    parameters.get( "receiver_email" ),
                    parameters.get( "payer_email" ) );


            // Throw an exception where failed so that the paypal knows the call failed with a 500 error
            if( res.equals( "VERIFIED" ) ) {
                if( validate( details, expectedAmount, expectedCurrency, idChecker ) ) {
                    if( details.isCompleted() ) {
                        onSuccess.run( details );
                    } else if( details.isPending() ) {
                        onPending.run( details );
                    } else {
                        throw new InvalidNotificationException( "Invalid payment status: " + details.paymentStatus);
                    }
                    return details;
                } else {
                    log.warn( "validation failure" );
                    onFailure.run( details );
                    throw new InvalidNotificationException( "IPN call failed validation" );
                }
            } else {
                if( res.equals( "INVALID" ) ) {
                    log.warn( "recieved response: " + res );
                } else {
                    log.error( "received an unknown response: " + res );
                }
                onFailure.run( details );
                throw new InvalidNotificationException( "invalid IPN call" );
            }
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }


    }

    private boolean validate( Details details, BigDecimal requiredAmount, String expectedCurrency, TransactionIdChecker idChecker ) {
        log.debug( "validate: " + details.paymentStatus + " - " + details.paymentAmount + " - " + details.paymentCurrency );

        // check that txnId has not been previously processed
        if( idChecker.hasBeenUsed( details.txnId)) {
            log.warn( "ipn validation failed because the txnId has been used before: " + details.txnId);
            return false;
        }

        // check that receiverEmail is your Primary PayPal email
        if( !same( details.receiverEmail, receiverEmail ) ) {
            log.warn( "ipn validation failed because receiverEmail is not " + receiverEmail + ". Is " + details.receiverEmail );
            return false;
        }
        // check that paymentAmount/paymentCurrency are correct
        try {
            BigDecimal ipnAmount = details.amount().setScale( 2 ); // ensure same scale as compared to
            if( ipnAmount == null ) {
                log.warn( "ipn validation failed because the amount is blank" );
                return false;
            }
            if( !ipnAmount.equals( requiredAmount.setScale( 2) ) ) { // ensure same scale as compared to
                log.warn( "ipn validation failed because the amount is not equal to the required amount. required=" + requiredAmount.toPlainString() + " actual=" + ipnAmount.toPlainString() );
                return false;
            }
            if( !same( details.paymentCurrency, expectedCurrency ) ) {
                log.warn( "ipn validation failed because the payment was not in the expected currency. required=" + expectedCurrency + " actual=" + details.paymentCurrency );
                return false;
            }
        } catch( Exception ex ) {
            log.warn( "ipn validation failed because i couldnt parse the amount", ex );
            return false;
        }

        return true;
    }

    private boolean same( String s1, String s2 ) {
        if( s1 == null ) return false;
        return s1.equals( s2 );
    }
}
