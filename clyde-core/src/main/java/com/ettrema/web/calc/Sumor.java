package com.ettrema.web.calc;

import com.ettrema.web.Templatable;
import java.math.BigDecimal;

class Sumor implements Accumulator {

    int decimals;

    public Sumor(int decimals) {
        super();
        this.decimals = decimals;
    }
    BigDecimal value = new BigDecimal(0);

    @Override
    public void accumulate(Templatable r, Object o) {
        BigDecimal bd = Calc.toBigDecimal(o, decimals);
        if (bd != null) {
            value = value.add(bd);
        }
    }
}
