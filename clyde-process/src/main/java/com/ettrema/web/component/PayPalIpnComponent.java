package com.ettrema.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.ettrema.pay.CreditManager;
import com.ettrema.pay.paypal.Details;
import com.ettrema.pay.paypal.InstantPaymentNotificationProcessor;
import com.ettrema.pay.paypal.InvalidNotificationException;
import com.ettrema.pay.paypal.PostPaymentRunner;
import com.ettrema.pay.paypal.TransactionIdChecker;
import com.ettrema.process.ProcessDef;
import com.ettrema.process.TokenValue;
import com.ettrema.vfs.VfsCommon;
import com.ettrema.web.Component;
import com.ettrema.web.Host;
import com.ettrema.web.RenderContext;
import com.ettrema.web.RequestParams;
import com.ettrema.web.Templatable;
import com.ettrema.context.RequestContext;
import java.math.BigDecimal;
import java.util.Map;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class PayPalIpnComponent extends VfsCommon implements Component, Addressable {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PayPalIpnComponent.class);
	private static final long serialVersionUID = 1L;
	public static final String RECEIVER_EMAIL_PARAM = "receiver_email";
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
		name = InitUtils.getValue(el, "name");
		amount = InitUtils.getBigDecimal(el, "amount");
		currency = InitUtils.getValue(el, "currency");
		productCode = InitUtils.getValue(el, "productCode");
		description = InitUtils.getValue(el, "description");
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public void populateXml(Element e2) {
		e2.setAttribute("class", getClass().getName());
		e2.setAttribute("name", name);
		InitUtils.setString(e2, "currency", currency);
		InitUtils.set(e2, "amount", this.amount);
		InitUtils.setString(e2, "productCode", this.productCode);
		InitUtils.setString(e2, "description", this.description);
	}

	public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
		log.debug("process");
		if (!parameters.containsKey(RECEIVER_EMAIL_PARAM)) {
			return null;
		}

		log.debug("process - doing ipn validation");
		InstantPaymentNotificationProcessor ipn = RequestContext.getCurrent().get(InstantPaymentNotificationProcessor.class);
		if (ipn == null) {
			throw new RuntimeException("No InstantPaymentNotificationProcessor configured");
		}

		Templatable targetPage = rc.getTargetPage();
		IpnSuccessRunner suc = new IpnSuccessRunner(targetPage);
		IpnFailureRunner fail = new IpnFailureRunner(targetPage);
		IpnPendingRunner pending = new IpnPendingRunner(targetPage);

		TransactionIdChecker idChecker = new MockTransactionIdChecker();
		try {
			ipn.process(parameters, amount, currency, suc, fail, pending, idChecker);
		} catch (InvalidNotificationException ex) {
			// throw an exception to generate a 500, so paypal knows this wasnt processed correctly
			throw new RuntimeException(ex);
		}
		return null;
	}

	abstract class IpnBase implements PostPaymentRunner {

		final Templatable targetPage;

		public IpnBase(Templatable targetPage) {
			this.targetPage = targetPage;
		}
	}

	class IpnSuccessRunner extends IpnBase {

		public IpnSuccessRunner(Templatable targetPage) {
			super(targetPage);
		}

		public void run(Details details) {
			log.debug("ipn success! creating credit");
			// create receipt in host/receipts
			createCredit(targetPage, details);
			
			// call scan on processdef
			if (!ProcessDef.scan(targetPage.getHost())) {
				log.error("Received payment, but no transition occurred!!! - targetPage: " + targetPage.getHref());
			} else {
				log.debug("payment received, credit created, and process transitioned");
				commit();
			}
		}

		private void createCredit(Templatable targetPage, Details details) {
			Host host = targetPage.getHost();
			if (host == null) {
				throw new RuntimeException("no host for: " + targetPage.getName());
			}

			_(CreditManager.class).createCredit(host, amount, "TEST", "test description");
			host.commit();
		}
	}

	class IpnPendingRunner extends IpnBase {

		public IpnPendingRunner(Templatable targetPage) {
			super(targetPage);
		}

		public void run(Details details) {
			log.debug("ipn pending: " + targetPage.getHref());
			TokenValue token = (TokenValue) targetPage.getParent().getParent();
			token.getVariables().put("pending", Boolean.TRUE);
			token.save();
			commit();
		}
	}

	class IpnFailureRunner extends IpnBase {

		public IpnFailureRunner(Templatable targetPage) {
			super(targetPage);
		}

		public void run(Details details) {
			log.warn("ipn validation failure: " + details);
		}
	}

	class MockTransactionIdChecker implements TransactionIdChecker {

		public boolean hasBeenUsed(String txId) {
			return false; // TODO
		}
	}

	public void init(Addressable container) {
		this.container = container;
	}

	public Addressable getContainer() {
		return container;
	}

	public boolean validate(RenderContext rc) {
		return true;
	}

	public String render(RenderContext rc) {
		return "";
	}

	public String renderEdit(RenderContext rc) {
		return "";
	}

	public String getName() {
		return name;
	}

	public void onPreProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
	}

	public Element toXml(Addressable container, Element el) {
		Element e2 = new Element("component");
		el.addContent(e2);
		populateXml(e2);
		return e2;
	}

	public Path getPath() {
		return container.getPath().child(name);
	}

	public final void setValidationMessage(String s) {
		RequestParams params = RequestParams.current();
		params.attributes.put(this.getName() + "_validation", s);
	}

	public final String getValidationMessage() {
		RequestParams params = RequestParams.current();
		return (String) params.attributes.get(this.getName() + "_validation");
	}
}
