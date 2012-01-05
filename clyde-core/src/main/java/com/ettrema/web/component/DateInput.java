package com.ettrema.web.component;

import java.text.ParseException;
import java.util.Date;
import org.jdom.Element;

public class DateInput extends AbstractInput<Date>{
       
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DateInput.class);
    
    private static final long serialVersionUID = 1L;
    
    private boolean hasTime;
    
    public DateInput(Addressable container,String name) {
        super(container,name);
    }

    public DateInput(Addressable container, Element el) {
        super(container,el);
        hasTime = InitUtils.getBoolean( el, "hasTime");
    }

    @Override
    public Element toXml(Addressable container, Element el) {
        Element e2 = super.toXml(container, el);
        InitUtils.set( e2, "hasTime", hasTime);
        return e2;
    }

    @Override
    public void fromXml(Element el) {
        super.fromXml(el);
    }
    
    
    
    
    @Override
    protected String editTemplate() {        
        String tm = hasTime ? " %H:%M" : "";
        return "<input type='text' name='${path}' id='${path}' value='${formattedValue}' />\n"
                + "<script type='text/javascript'>\n"
                + "Calendar.setup({\n"
                + "inputField     :    '${path}',   // id of the input field\n"
                + "ifFormat       :    '%d/%m/%Y" + tm + "',       // format of the input field\n"
                + "showsTime      :    ${input.hasTime},\n"
                + "timeFormat     :    '24',\n"
                + "});"
                + "</script>\n";
        
    }    

    @Override
    protected Date parse(String s) {
        if( s == null || s.trim().length() == 0 ) return null;
        try {
            Date dt = DateDef.sdf(hasTime).parse(s);
            log.debug("parsed " + s + " -> " + dt);
            return dt;
        } catch (ParseException ex) {
            log.warn("couldnt parse date", ex);
            return null;
        }
    }    

    public boolean hasTime() {
        return hasTime;
    }

}
