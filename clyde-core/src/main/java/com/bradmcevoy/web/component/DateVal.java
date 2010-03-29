
package com.bradmcevoy.web.component;

import com.bradmcevoy.web.CommonTemplated;
import java.util.Date;
import org.jdom.Element;

public class DateVal extends ComponentValue {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DateVal.class);
    private static final long serialVersionUID = 1L;
    
    public DateVal(String name, Addressable parent) {
        super(name, parent);
    }

    public DateVal(Element el, CommonTemplated container) {
        super(el, container);
    }

    @Override
    public Date getValue() {
        return (Date) super.getValue();
    }     
}
