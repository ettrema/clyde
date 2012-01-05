package com.ettrema.web.calc;

import com.ettrema.web.BaseResourceList;
import com.ettrema.web.Templatable;

/**
 *
 * @author brad
 */
public class ListFilter implements Accumulator {

    BaseResourceList dest = new BaseResourceList();

    @Override
    public void accumulate( Templatable r, Object o ) {
        if( o instanceof Boolean ) {
            Boolean b = (Boolean) o;
            if( b.booleanValue() ) {
                dest.add( r );
            }
        }
    }
}
