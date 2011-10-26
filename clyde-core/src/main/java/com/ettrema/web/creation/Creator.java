package com.bradmcevoy.web.creation;

import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import java.io.InputStream;

/**
 *
 * @author brad
 */
public interface Creator {
    boolean accepts(String contentType);

    BaseResource createResource(Folder folder, String ct, InputStream in, String newName) throws ReadingException, WritingException;
}
