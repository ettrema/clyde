
package com.ettrema.pay;

import java.text.DecimalFormat;
import java.text.ParseException;

public class Expiry {

    private static final DecimalFormat numberFormat = new DecimalFormat("00");

    public static Expiry fromString(String s) throws ParseException{
        if( s == null ) return null;
        int pos = s.indexOf("\\");
        if( pos < 0 ) throw new ParseException("No backslash", pos);
        String month = s.substring(0,pos);
        String year = s.substring(pos+1);
        int iMonth = Integer.parseInt(month);
        int iYear = Integer.parseInt(year);
        return new Expiry(iMonth, iYear);
    }
    
    public final int month;
    public final int year;

    public Expiry(int month, int year) {
        this.month = month;
        this.year = year;
    }

    /**
     *
     * @return - the expiry in format MMYY
     */
    public String toPlainString() {
        return numberFormat.format(month) + numberFormat.format(year);
    }
}
