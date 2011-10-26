package com.bradmcevoy.web.calc;

import com.bradmcevoy.web.BaseResourceList;
import com.bradmcevoy.web.Templatable;

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
