package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.utils.AuthoringPermissionService;
import com.bradmcevoy.utils.FileUtils;
import com.bradmcevoy.web.security.PermissionChecker;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import com.bradmcevoy.web.velocity.VelocityInterpreter;
import com.ettrema.context.RequestContext;
import com.ettrema.vfs.NameNode;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import org.apache.velocity.VelocityContext;

import static com.ettrema.context.RequestContext._;

public class EditPage implements PostableResource, DigestResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( EditPage.class );

    public static final String EDIT_SUFFIX = ".edit";
    
    public static boolean isEditPath( Path path ) {
        if( path == null || path.getName() == null ) return false;
        return ( path.getName().endsWith( EDIT_SUFFIX ) );
    }

    public static Path getEditeePath( Path path ) {
        Path folder = path.getParent();
        Path editee = folder.child( path.getName().replace( EDIT_SUFFIX, "" ) );
        return editee;
    }
    final CommonTemplated editee;

    public EditPage( CommonTemplated editee ) {
        this.editee = editee;
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException {
        ITemplate template = editee.getTemplate();
        if( template == null ) {
            log.debug( "-- no template. using default" );
            String sTemplate = FileUtils.readResource( this.getClass(), "defaultEdit.vel" );
            VelocityContext vc = new VelocityContext();
            vc.put( "editee", editee );
            vc.put( "targetPage", this );
            vc.put( "params", params );
            RenderContext rc = new RenderContext( template, editee, null, true );
            vc.put( "renderContext", rc );
            VelocityInterpreter.evalToStream( sTemplate, vc, out );
            out.flush();
        } else {
            log.trace("generate edit page with template: " + template.getName());
            RenderContext rc = new RenderContext( template, editee, null, true );
            String s = template.render( rc );
            if( s == null ) {
                log.warn( "Got null content for editee: " + editee.getHref() );
                return;
            } else {
                out.write( s.getBytes() );
            }
        }
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    /** TODO: move editing logic to here from Page
     */
    @Override
    public String processForm( Map<String, String> parameters, Map<String, FileItem> files ) throws NotAuthorizedException {
        return editee.processForm( parameters, files );
    }

    @Override
    public boolean authorise( Request request, Method method, Auth auth ) {
        PermissionChecker permissionChecker = RequestContext.getCurrent().get( PermissionChecker.class );
        Role editingRole = _(AuthoringPermissionService.class).getEditRole( editee );
        return permissionChecker.hasRole( editingRole, this.editee, auth );
    }

    @Override
    public Long getMaxAgeSeconds( Auth auth ) {
        return null;
    }

    public void onDeleted( NameNode nameNode ) {
        throw new UnsupportedOperationException( "Not supported." );
    }

    @Override
    public String getName() {
        return editee.getName() + ".edit";
    }

    @Override
    public Object authenticate( String user, String password ) {
        return editee.authenticate( user, password );
    }

    @Override
    public Object authenticate( DigestResponse digestRequest ) {
        return editee.authenticate( digestRequest );
    }

	@Override
    public boolean isDigestAllowed() {
        return true;
    }





    @Override
    public String getRealm() {
        return editee.getRealm();
    }

    @Override
    public Date getModifiedDate() {
        return null;
    }

    @Override
    public String getContentType( String accepts ) {
        return "text/html";
    }

    @Override
    public String checkRedirect( Request request ) {
        //return null;
        if( editee instanceof ISubPage) {
            String s = editee.getParent().getHref();
            if( !s.endsWith("/")) s += "/";
            s =  s + ".edit";
            return s;
        } else {
            return null;
        }
    }
}
