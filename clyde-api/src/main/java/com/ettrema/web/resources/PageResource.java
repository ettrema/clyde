package com.ettrema.web.resources;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.web.template.PageTemplater;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class PageResource extends AbstractContentResource {

    private String title;
    private String body;

    public PageResource( FolderResource parentFolder, String newName ) {
        super( "text/html", parentFolder, newName );
    }

    public String getTitle() {
        return title;
    }

    public void setTitle( String title ) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody( String body ) {
        this.body = body;
    }

    public Long getMaxAgeSeconds( Auth auth ) {
        return null;
    }

    public Long getContentLength() {
        return null;
    }

    @Override
    protected AbstractContentResource copyInstance( FolderResource newParent ) {
        PageResource newPage = new PageResource( newParent, this.getName());
        return newPage;
    }

    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        _(PageTemplater.class).render( this, out, params);
    }
}
