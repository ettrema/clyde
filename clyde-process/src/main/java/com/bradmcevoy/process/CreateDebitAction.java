package com.bradmcevoy.process;

import com.bradmcevoy.pay.CreditManager;
import com.bradmcevoy.web.Host;
import java.math.BigDecimal;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class CreateDebitAction implements ActionHandler {


	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SetVariable.class);
	private static final long serialVersionUID = 1L;
	private BigDecimal amount;
	private String currency;
	private String productCode;
	private String description;

	public CreateDebitAction(Element el) {
		amount = new BigDecimal(el.getAttributeValue("amount"));
		currency = el.getAttributeValue("currency");
		productCode = el.getAttributeValue("productCode");
		description = el.getText();
	}

	@Override
	public void populateXml(Element el) {
		el.setAttribute("amount", amount.toPlainString());
		el.setAttribute("currency", currency);
		el.setAttribute("productCode", productCode);
		el.setText(description);
	}


	@Override
	public void process(ProcessContext context) {
		try {
			TokenValue tv = (TokenValue) context.getToken();

			Host host = tv.getHost();
			_(CreditManager.class).createDebit(host, amount, productCode, description);
		} catch (Throwable e) {
			throw new RuntimeException("Exception creating credit", e);
		}
	}
}
