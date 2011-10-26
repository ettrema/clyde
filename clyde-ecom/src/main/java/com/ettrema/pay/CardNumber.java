
package com.bradmcevoy.pay;

import java.text.ParseException;

public class CardNumber {
    public static CardNumber fromString(String s) throws ParseException{
        return new CardNumber(s);
    }

    private final String number;
    
    private CardNumber(String s) {
        this.number = s;
    }

    @Override
    public String toString() {
        return number;
    }
    
    
}
