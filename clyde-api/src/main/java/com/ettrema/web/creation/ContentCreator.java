package com.ettrema.web.creation;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;
import java.io.InputStream;

/**
 *
 * @author brad
 */
public interface ContentCreator {
    boolean accepts(String contentType);
    Resource createResource( CollectionResource folder, String ct, InputStream in, String newName ) throws ReadingException, WritingException;
}
