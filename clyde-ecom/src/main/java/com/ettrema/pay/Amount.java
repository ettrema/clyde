package com.ettrema.pay;

import java.math.BigDecimal;

/**
 *
 */
public class Amount {

    public enum Currency {
        CAD,  	// Canadian Dollar
        CHF, 	// Swiss Franc
        EUR, 	//Euro
        FRF, 	//French Franc
        GBP, 	//United Kingdom Pound
        HKD, 	//Hong Kong Dollar
        JPY, 	//Japanese Yen
        NZD, 	//New Zealand Dollar
        SGD, 	//Singapore Dollar
        USD, 	//United States Dollar
        ZAR, 	//Rand
        AUD, 	//Australian Dollar
        WST, 	//Samoan Tala
        VUV, 	//Vanuatu Vatu
        TOP, 	//Tongan Pa'anga
        SBD, 	//Solomon Islands Dollar
        PGK, 	//Papua New Guinea Kina
        MYR, 	//Malaysian Ringgit
        KWD, 	//Kuwaiti Dinar
        FJD, 	//Fiji Dollar
    }

    public final BigDecimal amount;
    public final Currency currency;

    public Amount(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

    

}
