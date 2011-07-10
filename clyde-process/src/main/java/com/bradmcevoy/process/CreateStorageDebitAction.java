package com.bradmcevoy.process;

import com.bradmcevoy.pay.CreditManager;
import com.bradmcevoy.web.Host;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class CreateStorageDebitAction implements ActionHandler {


	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SetVariable.class);
	private static final long serialVersionUID = 1L;
	private String currency;
	private String productCode;
	private String description;

	public CreateStorageDebitAction(Element el) {		
		currency = el.getAttributeValue("currency");
		productCode = el.getAttributeValue("productCode");
		description = el.getText();
	}

	@Override
	public void populateXml(Element el) {

		el.setAttribute("currency", currency);
		el.setAttribute("productCode", productCode);
		el.setText(description);
	}


	@Override
	public void process(ProcessContext context) {
		try {
			TokenValue tv = (TokenValue) context.getToken();

			Host host = tv.getHost();
			_(CreditManager.class).createStorageDebit(tv, host, productCode, description);
		} catch (Throwable e) {
			throw new RuntimeException("Exception creating credit", e);
		}
	}
}
