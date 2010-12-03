package com.bradmcevoy.web;

import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.DateVal;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.joda.time.DateTime;

/**
 * Handy functions exposes to rendering logic for formatting.
 *
 * @author brad
 */
public class Formatter {

    private static final Formatter theInstance = new Formatter();
    public static DateFormat sdfDateUk = new SimpleDateFormat( "dd/MM/yyyy" );

    public static Formatter getInstance() {
        return theInstance;
    }

    public BigDecimal toDecimal( Object o, int places ) {
        if( o == null ) {
            return BigDecimal.ZERO;
        } else if( o instanceof Double ) {
            Double d = (Double) o;
            return BigDecimal.valueOf( d ).setScale( places, RoundingMode.HALF_UP );
        } else if( o instanceof Integer ) {
            Integer i = (Integer) o;
            return BigDecimal.valueOf( i.longValue() ).setScale( places, RoundingMode.HALF_UP );
        } else if( o instanceof Float ) {
            Float f = (Float) o;
            return BigDecimal.valueOf( f.doubleValue() ).setScale( places, RoundingMode.HALF_UP );
        } else if( o instanceof String ) {
            String s = (String) o;
            s = s.trim();
            if( s.length() == 0 ) {
                return BigDecimal.ZERO;
            } else {
                try {
                    return new BigDecimal( s ).setScale( places, RoundingMode.HALF_UP );
                } catch( NumberFormatException numberFormatException ) {
                    throw new RuntimeException( "Non-numeric data: " + s );
                }
            }
        } else {
            throw new RuntimeException( "Unsupported value type, should be numeric: " + o.getClass() );
        }
    }

    public Double toDouble( Object o ) {
        if( o == null ) {
            return 0d;
        } else if( o instanceof String ) {
            String s = (String) o;
            s = s.trim();
            if( s.length() == 0 ) {
                return 0d;
            } else {
                try {
                    return Double.valueOf( s );
                } catch( NumberFormatException numberFormatException ) {
                    throw new RuntimeException( "Non-numeric data: " + s );
                }
            }
        } else if( o instanceof Double ) {
            return (Double) o;
        } else if( o instanceof Integer ) {
            Integer i = (Integer) o;
            return (double) i;
        } else if( o instanceof Float ) {
            Float f = (Float) o;
            return f.doubleValue();
        } else {
            throw new RuntimeException( "Unsupported value type, should be numeric: " + o.getClass() );
        }
    }

    public Long toLong( Object oLimit ) {
        Long limit;
        if( oLimit == null ) {
            limit = 0l;
        } else if( oLimit instanceof Long ) {
            limit = (Long) oLimit;
        } else if( oLimit instanceof Integer ) {
            int i = (Integer) oLimit;
            limit = (long) i;
        } else if( oLimit instanceof String ) {
            String s = (String) oLimit;
            limit = Long.parseLong( s );
        } else {
            throw new RuntimeException( "unsupported class: " + oLimit.getClass() );
        }
        return limit;
    }

    public int getYear( Object o ) {
        if( o == null || !( o instanceof Date ) ) return 0;
        Date dt = (Date) o;

        Calendar cal = Calendar.getInstance();
        cal.setTime( dt );
        return cal.get( Calendar.YEAR );
    }

    public int getMonth( Object o ) {
        if( o == null || !( o instanceof Date ) ) return 0;
        Date dt = (Date) o;

        Calendar cal = Calendar.getInstance();
        cal.setTime( dt );
        return cal.get( Calendar.MONTH ) + 1;
    }

    public int getDayOfMonth( Object o ) {
        if( o == null || !( o instanceof Date ) ) return 0;
        Date dt = (Date) o;

        Calendar cal = Calendar.getInstance();
        cal.setTime( dt );
        return cal.get( Calendar.DAY_OF_MONTH ) + 1;
    }


    public String formatDate( Object o ) {
        if( o == null ) {
            return "";
        } else if( o instanceof Date ) {
            return sdfDateUk.format( o );
        } else if( o instanceof ComponentValue ) {
            DateVal dv = (DateVal) o;
            return formatDate( dv.getValue() );
        } else {
            throw new RuntimeException( "Unsupported type: " + o.getClass() );
        }
    }

    public org.joda.time.DateTime getDateTime( Object o ) {
        if( o == null ) return null;
        return new DateTime( o );
    }
}
