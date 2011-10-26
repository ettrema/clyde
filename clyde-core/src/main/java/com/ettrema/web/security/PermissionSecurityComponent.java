package com.ettrema.web.security;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.Component;
import com.ettrema.web.RenderContext;
import com.ettrema.web.RequestParams;
import com.ettrema.web.component.Addressable;
import com.ettrema.context.RequestContext;
import java.io.Serializable;
import java.util.Map;
import org.jdom.Element;

/**
 * 
 * Implements page level security. Determines required role based on the method
 * 
 * This is not applicable for POST, as then security contraints depend on which
 * component, if any, is being invoked.
 *
 *
 * @author brad
 */
public class PermissionSecurityComponent implements Component, Serializable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( PermissionSecurityComponent.class );
    private static final long serialVersionUID = 1L;
    private String name;
    private Addressable container;

    @Override
    public void init( Addressable container ) {
        this.container = container;
    }

    @Override
    public Addressable getContainer() {
        return container;
    }

    @Override
    public boolean validate( RenderContext rc ) {
        return true;
    }

    @Override
    public String render( RenderContext rc ) {
        log.debug( "render" );
        Resource resource = rc.getTargetPage();
        Request request = (Request) rc.getAttribute( "request" );
        if( request == null )
            throw new RuntimeException( "expected to find request in RenderContext attribute" );

        PermissionsAuthoriser permissionsAuthoriser = RequestContext.getCurrent().get( PermissionsAuthoriser.class );
        if( permissionsAuthoriser == null )
            throw new IllegalStateException( "Not found in configuration: " + PermissionsAuthoriser.class );


        Boolean b = permissionsAuthoriser.authorise( resource, request, request.getMethod(), request.getAuthorization() );
        log.debug( "result: " + b );
        if( b == null ) return null;
        else return b.toString();
    }

    @Override
    public String renderEdit( RenderContext rc ) {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String onProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        return null;
    }

    @Override
    public void onPreProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
    }

    public void fromXml( Element el ) {
        name = el.getAttributeValue( "name" );
    }

    @Override
    public Element toXml( Addressable container, Element el ) {
        Element e2 = new Element( "component" );
        el.addContent( e2 );
        e2.setAttribute( "name", name );
        e2.setAttribute( "class", this.getClass().getName() );
        return e2;
    }

    public final void setValidationMessage( String s ) {
        RequestParams params = RequestParams.current();
        params.attributes.put( this.getName() + "_validation", s );
    }

    public final String getValidationMessage() {
        RequestParams params = RequestParams.current();
        return (String) params.attributes.get( this.getName() + "_validation" );
    }
}
