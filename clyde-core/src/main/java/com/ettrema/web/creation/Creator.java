package com.ettrema.web.creation;

import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import java.io.InputStream;

/**
 *
 * @author brad
 */
public interface Creator {
    boolean accepts(String contentType);

    BaseResource createResource(Folder folder, String ct, InputStream in, String newName) throws ReadingException, WritingException;
}
