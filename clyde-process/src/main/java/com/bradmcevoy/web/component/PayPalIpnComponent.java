package com.bradmcevoy.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.context.RequestContext;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.pay.Credit;
import com.bradmcevoy.pay.paypal.Details;
import com.bradmcevoy.pay.paypal.InstantPaymentNotificationProcessor;
import com.bradmcevoy.pay.paypal.InvalidNotificationException;
import com.bradmcevoy.pay.paypal.PostPaymentRunner;
import com.bradmcevoy.pay.paypal.TransactionIdChecker;
import com.bradmcevoy.process.PaidRule;
import com.bradmcevoy.process.ProcessDef;
import com.bradmcevoy.process.TokenValue;
import com.bradmcevoy.utils.ClydeUtils;
import com.bradmcevoy.vfs.VfsCommon;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.Templatable;
import java.math.BigDecimal;
import java.util.Map;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class PayPalIpnComponent extends VfsCommon implements Component, Addressable{
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PayPalIpnComponent.class);
    private static final long serialVersionUID = 1L;

    private String name;
    private Addressable container;
    private BigDecimal amount;
    private String currency;
    private String productCode;
    private String description;

    public PayPalIpnComponent(Addressable container, String name) {
        this.container = container;
        this.name = name;
    }

    public PayPalIpnComponent(Addressable container, Element el) {
        this.container = container;
        name = InitUtils.getValue( el, "name");
        amount = InitUtils.getBigDecimal(el,"amount");
        currency = InitUtils.getValue(el,"currency");
        productCode = InitUtils.getValue(el,"productCode");
        description = InitUtils.getValue(el,"description");
    }

    public void populateXml(Element e2) {
        e2.setAttribute("class",getClass().getName());
        e2.setAttribute("name",name);
        InitUtils.setString( e2, "currency", currency);
        InitUtils.set( e2, "amount", this.amount);
        InitUtils.setString( e2, "productCode", this.productCode);
        InitUtils.setString( e2, "description", this.description);
    }

    public String onProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        log.debug( "process");
        if(!parameters.containsKey( "receiver_email")) return null;

        log.debug( "process - doing ipn validation");
        InstantPaymentNotificationProcessor ipn = RequestContext.getCurrent().get( InstantPaymentNotificationProcessor.class);
        if( ipn == null)throw new RuntimeException( "No InstantPaymentNotificationProcessor configured");

        Templatable targetPage = rc.getTargetPage();
        IpnSuccessRunner suc = new IpnSuccessRunner( targetPage);
        IpnFailureRunner fail = new IpnFailureRunner( targetPage );
        IpnPendingRunner pending = new IpnPendingRunner( targetPage );

        TransactionIdChecker idChecker = new MockTransactionIdChecker();
        try {
            ipn.process( parameters, amount, currency, suc, fail, pending, idChecker );
        } catch( InvalidNotificationException ex ) {
            // throw an exception to generate a 500, so paypal knows this wasnt processed correctly
            throw new RuntimeException( ex );
        }
        return null;
    }

    abstract class IpnBase implements PostPaymentRunner {
        final Templatable targetPage;

        public IpnBase( Templatable targetPage ) {
            this.targetPage = targetPage;
        }
    }

    class IpnSuccessRunner extends  IpnBase {

        public IpnSuccessRunner( Templatable targetPage ) {
            super(targetPage);
        }

        public void run(Details details) {
            log.debug( "ipn success! creating credit");
            // create receipt in host/receipts
            Credit credit = createCredit(targetPage, details);
            log.debug( "credit: " + credit.getHref());
            // call scan on processdef
            if( !ProcessDef.scan( targetPage.getHost()) ) {
                log.error( "Received payment, but no transition occurred!!! Credit: " + credit.getNameNodeId() + " - targetPage: " + targetPage.getHref());
            } else {
                log.debug( "payment received, credit created, and process transitioned");
                commit();
            }
        }

        private Credit createCredit( Templatable targetPage , Details details) {
            Host host = targetPage.getHost();
            if( host == null ) throw new RuntimeException( "no host for: " + targetPage.getName());

            Folder folder = PaidRule.getReceiptsFolder( host,true );
            String newName = ClydeUtils.getDateAsNameUnique( folder );
            Credit credit = Credit.create( folder, newName, amount, details.paymentCurrency, details.payerEmail, productCode, description );
            return credit;
        }

    }

    class IpnPendingRunner extends  IpnBase {
        public IpnPendingRunner( Templatable targetPage ) {
            super( targetPage );
        }
        public void run(Details details) {
            log.debug("ipn pending: " + targetPage.getHref());
            TokenValue token = (TokenValue) targetPage.getParent().getParent();
            token.getVariables().put( "pending", Boolean.TRUE);
            token.save();
            commit();
        }
    }

    class IpnFailureRunner extends  IpnBase {
        public IpnFailureRunner( Templatable targetPage ) {
            super( targetPage );
        }
        public void run(Details details) {
            log.warn("ipn validation failure: " + details);
        }
    }

    class MockTransactionIdChecker implements TransactionIdChecker {

        public boolean hasBeenUsed( String txId ) {
            return false; // TODO
        }

    }



    public void init( Addressable container ) {
        this.container = container;
    }

    public Addressable getContainer() {
        return container;
    }

    public boolean validate( RenderContext rc ) {
        return true;
    }

    public String render( RenderContext rc ) {
        return "";
    }

    
    public String renderEdit( RenderContext rc ) {
        return "";
    }

    public String getName() {
        return name;
    }


    public void onPreProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        
    }

    public Element toXml( Addressable container, Element el ) {
        Element e2 = new Element("component");
        el.addContent(e2);
        populateXml(e2);
        return e2;
    }


    public Path getPath() {
        return container.getPath().child(name);
    }

}
