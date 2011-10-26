
package com.ettrema.web;

import com.bradmcevoy.common.ContentTypeUtils;
import com.bradmcevoy.common.Path;
import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class ContentTypeUtil {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ContentTypeUtil.class );

    public static String getContentTypeString(String fileName) {
        return ContentTypeUtils.findContentTypes( fileName );
    }

    public static Iterable<Path> getContentTypeList(String fileName) {
        Collection mimeTypes = MimeUtil.getMimeTypes( fileName );
        ArrayList<Path> i = new ArrayList<Path>();
        HashSet<Path> set = new HashSet<Path>();

        for( Object o : mimeTypes ) {
            MimeType mt = (MimeType) o;
            Path p = Path.path(mt.toString());
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
