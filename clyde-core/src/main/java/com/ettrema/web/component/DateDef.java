package com.ettrema.web.component;

import com.ettrema.web.RenderContext;
import com.ettrema.web.Templatable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jdom.Element;

/**
 *
 * Maybe use this one - http://trentrichardson.com/examples/timepicker/
 *
 * @author brad
 */
public class DateDef extends TextDef {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( DateDef.class );
    private static final long serialVersionUID = 1L;
    private boolean hasTime = false;
    public  static final ThreadLocal<DateFormat> sdfDateOnly = new ThreadLocal<DateFormat>() {

        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat( "dd/MM/yyyy" );
        }
    };
    public static final ThreadLocal<DateFormat> sdfDateAndTime = new ThreadLocal<DateFormat>() {

        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat( "dd/MM/yyyy HH:mm" );
        }
    };

    public static Date parseValue( String s ) {
        if( s == null || s.trim().length() == 0 ) {
            return null;
        }
        try {
			Date dt;
			if( s.contains(":")) {
				dt = sdf(true).parse( s );
			} else {
				dt = sdf(false).parse( s );
			}
            return dt;
        } catch( ParseException ex ) {
            log.warn( "couldnt parse date", ex );
            return null;
//            throw new RuntimeException(ex);
        }
    }

    public DateDef( Addressable container, String name ) {
        super( container, name );
    }

    public DateDef( Addressable container, Element el ) {
        super( container, el );
        hasTime = InitUtils.getBoolean( el, "hasTime" );
    }

    @Override
    public Element toXml( Addressable container, Element el ) {
        Element e2 = super.toXml( container, el );
        InitUtils.set( e2, "hasTime", hasTime);
        return e2;
    }



    @Override
    public String render( ComponentValue c, RenderContext rc ) {
        return formatValue( c.getValue() );
    }

    public static DateFormat sdf(boolean hasTime) {
        if( hasTime ) {
            return sdfDateAndTime.get();
        } else {
            return sdfDateOnly.get();
        }
    }

    private DateFormat sdf() {
        return sdf(hasTime);
    }

    @Override
    public Date parseValue( ComponentValue cv, Templatable ct, String s ) {
        if( s == null || s.trim().length() == 0 ) {
            return null;
        }
        try {
            Date dt;        
            if( s.contains(":")) { // if there is no time component defintely dont parse with the date/time pattern
                dt = sdf().parse( s );
            } else {
                dt = sdf(false).parse( s );
            }
            return dt;
        } catch( ParseException ex ) {
            log.warn( "couldnt parse date", ex );
            return null;
//            throw new RuntimeException(ex);
        }
    }

    @Override
    public String formatValue( Object v ) {
        if( v == null ) {
            return "";
        } else if( v instanceof Date ) {
            String s = sdf().format( v );
            return s;
        } else {
            String s = v.toString();
            return s;
        }
    }

    @Override
    protected String editChildTemplate() {
        String reqClass = this.isRequired() ? "class='required'" : "";
        String template = "<input autocomplete='off' type='text' " + reqClass + " name='${path}' id='${path}' value='${val.formattedValue}' />\n"
            + "<script type='text/javascript'>\n"
            + "Calendar.setup({\n"
            + "inputField     :    '${path}',   // id of the input field\n"
            + "ifFormat       :    '%d/%m/%Y %H:%M',       // format of the input field\n"
            + "showsTime      :    ${def.showTime},\n"
            + "timeFormat     :    '24'"
            + "});"
            + "</script>\n";
        template = template + "#if($cv.validationMessage)";
        template = template + "<div class='validationError'>${cv.validationMessage}</div>";
        template = template + "#end";
        return template;


    }

    public boolean getShowTime() {
        return hasTime;
    }

    public void setShowTime(boolean b) {
        hasTime = b;
    }

    @Override
    public ComponentValue createComponentValue( Templatable newPage ) {
        DateVal cv = new DateVal( name.getValue(), null );
        return cv;
    }
}
