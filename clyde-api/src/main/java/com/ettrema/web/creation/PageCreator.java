package com.ettrema.web.creation;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.ettrema.web.resources.FolderResource;
import com.ettrema.web.resources.PageResource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 *
 * @author brad
 */
public class PageCreator implements ContentCreator {

    @Override
    public boolean accepts( String contentType ) {
        return contentType.contains( "html" );
    }

    @Override
    public Resource createResource( CollectionResource folder, String ct, InputStream in, String newName ) throws ReadingException, WritingException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        StreamUtils.readTo( in, bout );

        PageResource page = new PageResource( (FolderResource) folder, newName );
        page.setBody( bout.toString() );
        page.save();
        return page;
    }
}
