package com.ettrema.web.component;

import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Templatable;
import java.util.Date;
import org.jdom.Element;
import org.joda.time.DateTime;

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
        Object oVal = super.getValue();
        if (oVal == null) {
            return null;
        } else if (oVal instanceof Date) {
            return (Date) oVal;
        } else {
            log.debug("Date value is not of type date: " + oVal.getClass());
            if (oVal instanceof String) {
                String s = (String) oVal;
                Templatable ct = (Templatable) getParent();
                ComponentDef def = this.getDef(ct);
                if (def == null) {
                    return null;
                } else {
                    if (def instanceof DateDef) {
                        return (Date) def.parseValue(this, ct, s);
                    } else {
                        return null;
                    }
                }
            } else {
                return null;
            }
        }
    }

    public org.joda.time.DateTime getJodaDate() {
        Date dt = getValue();
        if( dt != null ) {
            return new DateTime(dt.getTime());
        } else {
            return null;
        }
    }

    public Date getNow() {
        return new Date();
    }

    public boolean isFuture() {
        Date dt = getValue();
        if (dt == null) {
            return false;
        } else {
            return dt.after(getNow());
        }
    }
}
