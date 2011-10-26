package com.bradmcevoy.pay.paypal;

import com.ettrema.context.Context;
import com.ettrema.context.Factory;
import com.ettrema.context.Registration;
import com.ettrema.context.RootContext;


/**
 *
 * @author brad
 */
public class InstantPaymentNotificationProcessorFactory implements Factory<InstantPaymentNotificationProcessor>  {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InstantPaymentNotificationProcessorImpl.class);

    public static Class[] classes = {InstantPaymentNotificationProcessor.class};

    private String payPalUrl = "https://www.paypal.com/cgi-bin/webscr";
    // alternative "http://test.bradsphotobackup.com/cgi-bin/webscr"

    private String receiverEmail;

    public Class[] keyClasses() {
        return classes;
    }

    public String[] keyIds() {
        return null;
    }
    

    public Registration<InstantPaymentNotificationProcessor> insert( RootContext rootContext, Context requestContext ) {
        log.debug( "creating " + InstantPaymentNotificationProcessorImpl.class.getCanonicalName() + " on paypayurl: " + payPalUrl);
        InstantPaymentNotificationProcessor impl = new InstantPaymentNotificationProcessorImpl(payPalUrl, receiverEmail);
        Registration<InstantPaymentNotificationProcessor> reg = rootContext.put(impl,this);
        return reg;
    }

    public void init( RootContext context ) {
        
    }

    public void destroy() {
        
    }

    public void onRemove( InstantPaymentNotificationProcessor item ) {
        
    }

    public void setPayPalUrl( String payPalUrl ) {
        this.payPalUrl = payPalUrl;
    }

    public void setReceiverEmail( String receiverEmail ) {
        this.receiverEmail = receiverEmail;
    }

    


}
