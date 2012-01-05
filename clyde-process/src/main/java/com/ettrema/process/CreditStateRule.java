package com.ettrema.process;

import com.bradmcevoy.process.ProcessContext;
import com.bradmcevoy.process.Rule;
import java.io.Serializable;
import com.ettrema.web.Host;
import com.ettrema.pay.CreditManager;
import java.math.BigDecimal;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class CreditStateRule implements Rule, Serializable {

	private static final long serialVersionUID = 1L;
	
	private boolean  triggerOnPositive;

	public CreditStateRule(Element el) {
		String s = el.getAttributeValue("triggerOnPositive");
		if( s == null || s.length() == 0 || s.equalsIgnoreCase("true")) {
			triggerOnPositive = true;
		} else {
			triggerOnPositive = false;
		}
	}
	
	
	public void arm(ProcessContext context) {
	}

	public void disarm(ProcessContext context) {
	}

	public boolean eval(ProcessContext context) {
		TokenValue tv = (TokenValue) context.getToken();
		Host host = tv.getHost();
		BigDecimal balance = _(CreditManager.class).calcBalance(host);
		if( triggerOnPositive ) {
			if( balance.compareTo(BigDecimal.ZERO) > 0 ) {
				return true;
			}
		} else {
			if( balance.compareTo(BigDecimal.ZERO) < 0 ) {
				return true;
			}			
		}
		return false;
	}

	public void populateXml(Element elRule) {
		elRule.setAttribute("triggerOnPositive", triggerOnPositive ? "true" : "false");							
	}
}
