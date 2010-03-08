
package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class ContentTypeUtil {
    /**
     * Parses the content type string and returns all parts of all included
     * content types as paths, order most to least specific
     * 
     * Eg image/jpeg,image/pjpeg returns..
     *  image/jpeg
     *  image/pjpeg
     *  image
     * 
     * @param ct
     * @return
     */
    public static Iterable<Path> splitContentTypeList(String ct) {
        ArrayList<Path> i = new ArrayList<Path>();
        HashSet<Path> set = new HashSet<Path>();
        for( String pair : ct.split("[,]")) {
            Path p = Path.path(pair);
            while( p != null && !p.isRoot() ) {
                if( !set.contains(p)) {
                    i.add(p);
                    set.add(p);
                }
                p = p.getParent();
            }
        }
        Collections.sort(i, Path.LENGTH_COMPARATOR);
        Collections.reverse(i);
        return i;
    }
    
}
