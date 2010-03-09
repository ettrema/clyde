package com.bradmcevoy.process;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.pay.Credit;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.SubPage;
import com.bradmcevoy.web.Templatable;
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
    
    public static final String RECEIPTS_FOLDER = "_receipts";



    public static Folder getReceiptsFolder( Host host, boolean autocreate ) {
        Folder f = getReceiptsFolder( host );
        if( autocreate ) {
            if( f == null ) {
                f = new Folder( host, RECEIPTS_FOLDER );
                f.save();
            }
        }
        return f;
    }

    public static Folder getReceiptsFolder( Host host ) {
        Resource r = host.child( "_receipts" );
        if( r == null ) {
            log.debug( "no receipts folder in host: " + host.getName() );
            return null;
        } else if( r instanceof Folder ) {
                Folder receipts = (Folder) r;
                return receipts;
            } else {
                log.debug( "receipts folder is not a folder! " + r.getName() + " - " + r.getClass() );
                return null;
            }
    }


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
        Folder receipts = getReceiptsFolder( host );
        if( receipts == null ) {
            log.debug( "no receipts folder" );
            return false;
        }
        List<Templatable> children = receipts.getChildren();
        if( children.size() == 0 ) {
            log.debug( "receipts folder contains no entries");
            return false;
        }
        for( Templatable ct : children ) {
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
        log.debug( "rule not met" );
        return false;

    }
}
