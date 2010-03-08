package com.bradmcevoy.utils;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author brad
 */
public class ClydeUtils {
    public static String getDateAsName() {
        Date dt = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime( dt );
        String name = cal.get( Calendar.YEAR) + "_" + cal.get( Calendar.MONTH) + "_" + cal.get( Calendar.DAY_OF_MONTH) + "_" + cal.get( Calendar.HOUR_OF_DAY) + cal.get( Calendar.MINUTE);
        return name;
    }

    public static String getDateAsNameUnique(CollectionResource col) {
        String name = getDateAsName();
        return getUniqueName( col, name );
    }

    public static String getUniqueName(CollectionResource col, String name) {
        Resource r = col.child( name );
        boolean isFirst = true;
        while( r != null ) {
            System.out.println( "incremeneint:" + name );
            name = com.bradmcevoy.io.FileUtils.incrementFileName( name, isFirst );
            System.out.println( "increatementd: " + name);
            isFirst = false;
            r = col.child( name );
        }
        return name;
    }

}
