package com.ettrema.web.code;

import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Generates the code representation of the content of a resource. This
 * is simply the same content for binary files, but might be a html representation
 * of the local content for a templated resource
 *
 * @author brad
 */
public interface ContentTypeHandler {

    /**
     * Return true if this can generate content for the given resource
     *
     * @param r
     * @return
     */
    boolean supports(Resource r);


    /**
     * Produce the code content for the given resource
     * 
     * @param out
     * @param wrapped
     */
    void generateContent( OutputStream out, GetableResource wrapped )throws IOException ;

    void replaceContent( InputStream in, Long contentLength, GetableResource wrapped );

    

}
