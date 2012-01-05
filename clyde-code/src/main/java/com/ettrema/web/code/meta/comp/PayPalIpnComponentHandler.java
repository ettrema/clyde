package com.ettrema.web.code.meta.comp;

import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Component;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.component.PayPalIpnComponent;
import java.math.BigDecimal;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class PayPalIpnComponentHandler implements ComponentHandler {

    public Class getComponentClass() {
        return PayPalIpnComponent.class;
    }

    public String getAlias() {
        return "paypalipn";
    }

    public Element toXml(Component c) {
        PayPalIpnComponent t = (PayPalIpnComponent) c;
        Element e2 = new Element(getAlias(), CodeMeta.NS);
        populateXml(e2, t);
        return e2;
    }

    public Component fromXml(CommonTemplated res, Element el) {
        String name = el.getAttributeValue("name");
        if (StringUtils.isEmpty(name)) {
            throw new RuntimeException("Empty component name");
        }
        PayPalIpnComponent text = new PayPalIpnComponent(res, name);
        fromXml(text, el);
        return text;
    }

    public void fromXml(PayPalIpnComponent fpc, Element el) {
        fpc.setAmount(InitUtils.getBigDecimal(el, "amount"));
        fpc.setCurrency(InitUtils.getValue(el, "currency"));
        fpc.setProductCode(InitUtils.getValue(el, "productCode"));
        fpc.setDescription(InitUtils.getValue(el, "description"));

    }

    public void populateXml(Element e2, PayPalIpnComponent t) {
        InitUtils.set(e2, "name", t.getName());

        InitUtils.setString(e2, "currency", t.getCurrency());
        InitUtils.set(e2, "amount", t.getAmount());
        InitUtils.setString(e2, "productCode", t.getProductCode());
        InitUtils.setString(e2, "description", t.getDescription());

    }
}
