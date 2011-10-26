package com.bradmcevoy.web.calc;

import com.bradmcevoy.web.Templatable;

interface Accumulator {

    void accumulate(Templatable r, Object o);
}
