package com.bradmcevoy.web.component;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.pay.CardNumber;
import com.bradmcevoy.pay.Expiry;
import com.bradmcevoy.pay.PaymentRequest;
import com.bradmcevoy.pay.PaymentService;
import com.bradmcevoy.process.ClydeTransition;
import com.bradmcevoy.process.ProcessContext;
import com.bradmcevoy.process.TokenValue;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.ComponentContainer;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.WrappedSubPage;
import java.text.ParseException;
import java.util.Map;
import org.jdom.Element;

public class PaymentCommand extends Command implements ComponentContainer {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PaymentCommand.class);
    private static final long serialVersionUID = 1L;
    private Text creditCard;
    private Text expires;
    private Text cardHolder;
    private Text address;
    private DecimalInput amount;

    public PaymentCommand(Addressable container, String name) {
        super(container, name);
        initNullFields();
    }

    public PaymentCommand(Addressable container, Element el) {
        super(container, el);
        InitUtils.initComponentFields(el, this);
        initNullFields();
    }

    private String doPayment(ProcessContext processContext, BaseResource pageToSave) throws ParseException {
        PaymentService paymentService = requestContext().get(PaymentService.class);
        if (paymentService == null) {
            throw new NullPointerException("No payment service in context");
        }
        CardNumber cardNum = CardNumber.fromString(creditCard.getValue());
        Expiry expiry = Expiry.fromString(expires.getValue());

        PaymentRequest req = new PaymentRequest(cardNum, expiry, this.address.getValue(), this.cardHolder.getValue(), this.amount.getValue());

        Addressable cn = this.getContainer();
        ClydeTransition tr = (ClydeTransition) unwrap(cn);

        throw new UnsupportedOperationException("not done yet");
//        boolean bOk = paymentService.doPayment(req);
//        if (!bOk) {
//            throw new RuntimeException("Payment failed");
//        } else {
//            log.debug("PAYMENT ok: " + this.amount.getValue());
//            boolean didTransition = processContext.fireTransition(tr.getName());
//            if (!didTransition) {
//                // TODO FIXME: this condition should be logged really well
//                log.error("payment processing failed" + this.getPath());
//                throw new RuntimeException("IMPORTANT: your payment appears to have succeeded but your order was not processed correctly. Please report this to us so we can process your order manually.");
//            } else {
//                // all good. save the page and redirect
//                pageToSave.save();
//                this.commit();
//                return pageToSave.getHref();
//            }
//        }
    }

    private void initNullFields() {
        if (creditCard == null) {
            creditCard = new Text(this, "creditCard");
        }
        creditCard.setRequestScope(true);
        if (expires == null) {
            expires = new Text(this, "expires");
        }
        expires.setRequestScope(true);
        if (cardHolder == null) {
            cardHolder = new Text(this, "cardHolder");
        }
        cardHolder.setRequestScope(true);
        if (address == null) {
            address = new Text(this, "address");
        }
        address.setRequestScope(true);
        if (amount == null) {
            amount = new DecimalInput(this, "amount");
        }
    }

    public Text getCreditCard() {
        return creditCard;
    }

    public Text getExpires() {
        return expires;
    }

    public Text getCardHolder() {
        return cardHolder;
    }

    public Text getAddress() {
        return address;
    }

    @Override
    public Element toXml(Addressable container, Element el) {
        log.debug("toXml");
        Element e2 = super.toXml(container, el);
        InitUtils.componentFieldsToXml(this, e2);
        return e2;
    }

    public boolean validate(RenderContext rc) {
        return ComponentUtils.validateComponents(this, rc);
    }

    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        String s = parameters.get(this.getName());
        if (s == null) {
            return null; // not this command
        }
        if (validate(rc)) {
            try {
                Addressable cn = this.getContainer();
                WrappedSubPage thisPage = (WrappedSubPage) cn;                
                ClydeTransition tr = (ClydeTransition)thisPage.unwrap();
                com.bradmcevoy.process.State fromState = tr.getFromState();
                com.bradmcevoy.process.Process process = fromState.getProcess();
                Templatable ct = rc.page;
                TokenValue token = getTokenValueFromTransition(ct);
                ProcessContext pc = new ProcessContext(token, process);
                BaseResource pageToSave = (BaseResource)token.getContainer();
                return doPayment(pc, pageToSave);
            } catch (ParseException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            log.debug("validation failed");
            return null;
        }
    }

    public TokenValue getTokenValueFromTransition(Templatable ctWrappedTransition) {
        WrappedSubPage wrappedState = parentFromWrapped(ctWrappedTransition);
        TokenValue tv = (TokenValue) parentFromWrapped(wrappedState).unwrap();
        return tv;
    }

    
    public WrappedSubPage parentFromWrapped(Templatable ct) {
        if (ct instanceof WrappedSubPage) {
            WrappedSubPage wsp = (WrappedSubPage) ct;
            return  (WrappedSubPage) wsp.getParent();
        } else {
            throw new RuntimeException("Expected the page to be a WrappedSubPage, not a: " + ct.getClass().getName());
        }
    }
    
    public CommonTemplated unwrap(Addressable a) {
        if( a instanceof WrappedSubPage ) {
            return ((WrappedSubPage)a).unwrap();
        } else {
            return (CommonTemplated) a;
        }
    }

    public Component getAnyComponent(String name) {
        if (name.equals("creditCard")) {
            return creditCard;
        } else if (name.equals("expires")) {
            return expires;
        } else if (name.equals("cardHolder")) {
            return cardHolder;
        } else if (name.equals("address")) {
            return address;
        } else if (name.equals("amount")) {
            return amount;
        } else {
            return null;
        }
    }

    @Override
    protected String doProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
