
package com.ettrema.web;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Scratch {
    public static void main(String[] args) throws Exception {
            Double dd = Double.parseDouble( "0.0");
            BigDecimal bd = new BigDecimal( dd );
            bd = bd.setScale( 0, RoundingMode.HALF_UP);
            System.out.println( "bd: " + bd.toPlainString());
    }
}
