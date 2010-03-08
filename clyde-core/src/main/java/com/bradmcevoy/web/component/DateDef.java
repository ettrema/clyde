package com.bradmcevoy.web.component;

import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.Templatable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jdom.Element;

public class DateDef extends TextDef{
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DateDef.class);
    
    private static final long serialVersionUID = 1L;
    
    public static DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    
    BooleanInput showTime = new BooleanInput(this, "showTime");
    
    public DateDef(Addressable container,String name) {
        super(container,name);
    }
    
    public DateDef(Addressable container, Element el) {
        super(container,el);
        if( showTime == null ) showTime = new BooleanInput(this, "showTime");
        String s = el.getAttributeValue("showTime");
        showTime.setValue(s==null||!s.equals("true") ? false : true );
    }
    
    @Override
    public String render(ComponentValue c,RenderContext rc) {
        return formatValue(c.getValue());
    }

    @Override
    public Date parseValue(ComponentValue cv, Templatable ct,String s) {
        if( s == null || s.trim().length() == 0 ) return null;
        try {
            Date dt = sdf.parse(s);
//            log.debug("parsed " + s + " -> " + dt);
            return dt;
        } catch (ParseException ex) {
            log.warn("couldnt parse date", ex);
            return null;
//            throw new RuntimeException(ex);
        }
    }

    @Override
    public String formatValue(Object v) {
        if( v == null ) {
            return "";
        } else if( v instanceof Date ) {
            String s = sdf.format(v);
//            log.debug("formatted " + v + " -> " + s);
            return s;
        } else {
            String s = v.toString();
//            Date dt = parseValue(s);
//            if( dt == null ) return "";
            return s;
        }
    }
                
    @Override
    protected String editChildTemplate() {
        return "<input type='text' name='${path}' id='${path}' value='${val.formattedValue}' />\n"
                + "<script type='text/javascript'>\n"
                + "Calendar.setup({\n"
                + "inputField     :    '${path}',   // id of the input field\n"
                + "ifFormat       :    '%d/%m/%Y %H:%M',       // format of the input field\n"
                + "showsTime      :    ${def.showTime.value},\n"
                + "timeFormat     :    '24',\n"
                + "});"
                + "</script>\n";
    }

    public BooleanInput getShowTime() {
        if( showTime == null ) showTime = new BooleanInput(this, "showTime");
        return showTime;
    }

    @Override
    public ComponentValue createComponentValue(Templatable newPage) {
        DateVal cv = new DateVal(name.getValue(), null);
        return cv;
    }
    
    
}
