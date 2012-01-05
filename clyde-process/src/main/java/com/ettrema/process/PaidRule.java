package com.ettrema.process;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.process.ProcessContext;
import com.bradmcevoy.process.Rule;
import com.ettrema.pay.Credit;
import com.ettrema.pay.CreditManager;
import com.ettrema.web.Folder;
import com.ettrema.web.Host;
import com.ettrema.web.SubPage;
import com.ettrema.web.Templatable;
import java.util.List;
import org.jdom.Element;

/**
 * A rule which fires when there is an unused Credit in the Host's _receipts folder
 * which has the same product code as is defined in this class.
 *
 * Use something like PayPalIpnComponent to create the credit.
 *
 * @author brad
 */
public class PaidRule implements Rule {
    
    private static final long serialVersionUID =  1220458028615751318L;

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( PaidRule.class );
    


    String productCode;

    public PaidRule( Element el ) {
        this.productCode = el.getAttributeValue( "productCode" );
    }

    public void arm( ProcessContext context ) {
    }

    public void disarm( ProcessContext context ) {
    }

    public void populateXml( Element elRule ) {
        elRule.setAttribute( "productCode", productCode );
    }

    public boolean eval( ProcessContext context ) {
        log.debug( "eval" );
        Host host = ( (SubPage) context.token ).getHost();
        if( host == null )
            throw new RuntimeException( "couldnt find host for: " + context.token );
        return checkReceipt( host );
    }

    private boolean checkReceipt( Host host ) {
        log.debug( "checkReceipt" );
        Folder receipts = CreditManager.getReceiptsFolder( host );
        if( receipts == null ) {
            log.debug( "no receipts folder" );
            return false;
        }
        List<? extends Resource> children = receipts.getChildren();
        if( children.isEmpty() ) {
            log.debug( "receipts folder contains no entries");
            return false;
        }
        for( Resource r : children ) {
            if( r instanceof Templatable ) {
                Templatable ct = (Templatable) r;
                log.debug( "check credit: " + ct.getPath() );
                if( ct instanceof Credit ) {
                    Credit c = (Credit) ct;
                    if( !c.isUsed() ) {
                        log.debug( "found unused credit: " + c.getProductCode() );
                        if( c.getProductCode().equals( this.productCode ) ) {
                            log.debug( "found matching product code" );
                            c.setUsed( true );
                            c.save();
                            return true;
                        } else {
                            log.debug( "product codes don't match: " + c.getProductCode() + " != " + this.productCode);
                        }
                    }
                }
            }
        }
        log.debug( "rule not met" );
        return false;

    }
}
