package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.web.CommonTemplated.Params;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.component.ComponentDef;
import com.ettrema.context.RequestContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author brad
 */
public class WrappedTemplate implements ITemplate {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( WrappedTemplate.class );
    private final ITemplate physicalTemplate;
    private final Web web;

    /**
     *
     * @param t - the template to wrap. ie from a parent web
     * @param web - the web which the page using this template is in
     */
    public WrappedTemplate( ITemplate t, Web web ) {
        this.physicalTemplate = t;
        this.web = web;
    }

    @Override
    public Folder createFolderFromTemplate( Folder location, String name ) {
        return physicalTemplate.createFolderFromTemplate( location, name );
    }

    @Override
    public BaseResource createPageFromTemplate( Folder location, String name, InputStream in, Long length ) {
        return physicalTemplate.createPageFromTemplate( location, name, in, length );
    }

    @Override
    public BaseResource createPageFromTemplate( Folder location, String name ) {
        return physicalTemplate.createPageFromTemplate( location, name );
    }

    @Override
    public ComponentDef getComponentDef( String name ) {
        return physicalTemplate.getComponentDef( name );
    }

    @Override
    public ComponentDefMap getComponentDefs() {
        return physicalTemplate.getComponentDefs();
    }

    @Override
    public ITemplate getTemplate() {
        TemplateManager tm = RequestContext.getCurrent().get( TemplateManager.class );
        ITemplate t = tm.lookup( physicalTemplate.getTemplateName(), web );
        return t;
    }

    @Override
    public Collection<Component> allComponents() {
        return physicalTemplate.allComponents();
    }

    @Override
    public Component getAnyComponent( String childName ) {
        return physicalTemplate.getAnyComponent( childName );
    }

    @Override
    public boolean is( String type ) {
        return physicalTemplate.is( type );
    }

    @Override
    public String getUniqueId() {
        return physicalTemplate.getUniqueId();
    }

    @Override
    public String getName() {
        return physicalTemplate.getName();
    }

    @Override
    public Object authenticate( String user, String pwd ) {
        return web.authenticate( user, pwd );
    }

    @Override
    public boolean authorise( Request arg0, Method arg1, Auth arg2 ) {
        return web.authorise( arg0, arg1, arg2 );
    }

    @Override
    public String getRealm() {
        return web.getRealm();
    }

    @Override
    public Date getModifiedDate() {
        return physicalTemplate.getModifiedDate();
    }

    @Override
    public Date getCreateDate() {
        return physicalTemplate.getCreateDate();
    }

    @Override
    public String checkRedirect( Request arg0 ) {
        return physicalTemplate.checkRedirect( arg0 );
    }

    @Override
    public Component _invoke( String name ) {
        return physicalTemplate._invoke( name );
    }

    @Override
    public String render( RenderContext child ) {
        log.debug( "wrapped template rending");
        ITemplate t = getTemplate();
        RenderContext rc = new RenderContext( t, this, child, false );
        if( t != null ) {
            log.debug( "wrapped template rending: " + t.getName());
            return t.render( rc );
        } else {
            log.debug( "wrapped template - template not found, try to render with root component" );
            Component cRoot = getParams().get( "root" );
            if( cRoot == null ) {
                log.warn( "no root component for template: " + this.getHref() );
                return "";
            } else {
                return cRoot.render( rc );
            }
        }

    }

    @Override
    public Component getComponent( String paramName, boolean includeValues ) {
        return physicalTemplate.getComponent( paramName, includeValues );
    }

    @Override
    public ComponentValueMap getValues() {
        return physicalTemplate.getValues();
    }

    @Override
    public ComponentMap getComponents() {
        return physicalTemplate.getComponents();
    }

    @Override
    public void preProcess( RenderContext rcChild, Map<String, String> parameters, Map<String, FileItem> files ) {
        physicalTemplate.preProcess( rcChild, parameters, files );
    }

    @Override
    public String getHref() {
        return web.getHref() + "/templates/" + physicalTemplate.getName();
    }

    @Override
    public String process( RenderContext rcChild, Map<String, String> parameters, Map<String, FileItem> files ) throws NotAuthorizedException {
        return this.physicalTemplate.process( rcChild, parameters, files );
    }

    @Override
    public String getTemplateName() {
        return physicalTemplate.getTemplateName();
    }

    @Override
    public Web getWeb() {
        return web;
    }

    @Override
    public Folder getParentFolder() {
        return web.getTemplates();
    }

    @Override
    public Templatable find( Path path ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Templatable getParent() {
        return getParentFolder();
    }

    @Override
    public Params getParams() {
        return physicalTemplate.getParams();
    }

    @Override
    public Addressable getContainer() {
        return this;
    }

    @Override
    public Path getPath() {
        return getParent().getPath().child( getName() );
    }

    @Override
    public Host getHost() {
        if( web instanceof Host ) {
            return (Host) web;
        } else {
            return web.getHost();
        }
    }

    @Override
    public boolean represents( String type ) {
        return this.physicalTemplate.represents( type );
    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public int compareTo( Resource o ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getContentType( String accepts ) {
        return physicalTemplate.getContentType( accepts );
    }

    public boolean canCreateFolder() {
        return physicalTemplate.canCreateFolder();
    }

    public void onAfterSave( BaseResource aThis ) {
        physicalTemplate.onAfterSave( aThis );
    }




}
