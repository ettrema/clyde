package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.utils.AuthoringPermissionService;
import com.bradmcevoy.utils.ClydeUtils;
import com.bradmcevoy.web.security.PermissionChecker;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;

public class NewPage implements PostableResource, XmlPersistableResource, DigestResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( NewPage.class );
    public static final String AUTO_NAME = "_autoname";
    public static String NEW_INDICATOR = ".new";

    public static boolean isNewPath( Path path ) {
        if( path == null || path.getName() == null ) {
            return false;
        }
        return ( path.getName().endsWith( ".new" ) );
    }

    static Path getPagePath( Path path ) {
        String nm = path.getName().replace( ".new", "" );
        return path.getParent().child( nm );
    }
    final Folder folder;
    final String newName;
    private transient BaseResource editee;
    private transient ITemplate template;

    public NewPage( Folder folder, String newName ) throws IllegalArgumentException {
        this.folder = folder;
        this.newName = newName;
        if( folder.child( newName ) != null ) {
            throw new IllegalArgumentException( "The file already exists" );
        }
//        if (newName.contains(" ") || newName.contains(",")) {
//            throw new IllegalArgumentException("The file name is invalid");
//        }
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public String getHref() {
        return folder.getHref() + newName + ".new";
    }

    private ITemplate getTemplate( Map<String, String> params ) {
        if( template == null ) {
            String templateName = templateName( params );
            template = folder.getTemplate( templateName );
            if( template == null ) {
                log.warn( "couldnt find template: " + templateName );
            }
        }
        return template;
    }

    private String templateName( Map<String, String> params ) {
        if( params == null ) {
            throw new NullPointerException( "no params" );
        }
        String t = selectName();
        if( t == null ) {
            return null;
        }
        return params.get( t );
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException( "Delete not possible on new page" );
    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        log.debug( "sendContent" );
        String templateName = templateName( params );
        if( templateName == null || templateName.length() == 0 ) {
            String s = renderSelectTemplate();
            out.write( s.getBytes() );
        } else {
            ITemplate template = getTemplate( params );
            if( template == null ) {
                throw new RuntimeException( "Template not found: " + templateName );
            }
            if( editee == null ) {
                editee = template.createPageFromTemplate( folder, newName );
            }
            if( editee instanceof EditableResource ) {
                EditableResource er = (EditableResource) editee;
                er.getEditPage().sendContent( out, range, params, null );
            } else {
                RenderContext rc = new RenderContext( template, editee, null, true );
                String s = template.render( rc );
                if( s == null ) {
                    log.warn( "Got null content for editee: " + editee.getHref() );
                    return;
                }
                out.write( s.getBytes() );
            }
        }
    }

    private String renderSelectTemplate() {
        StringBuffer sb = new StringBuffer();
        sb.append( "<html><body>" );
        sb.append( "<h1></h1>" );
        sb.append( "<h1>Select Template for New Page</h1>" );
        sb.append( "<ul>" );

        for( Template res : folder.getAllowedTemplates() ) {
            if( res instanceof Template ) {
                sb.append( "<li><a href='" ).append( this.getHref() ).append( "?" ).append( selectName() ).append( "=" ).append( res.getName() ).append( "'>" ).append( res.getName() ).append( "</a></li>" );
            }
        }
        sb.append( "</ul>" );
        sb.append( "</body></html>" );
        return sb.toString();
    }

    public static String selectName() {
        return "templateSelect";
//        return folder.getPath().child("new").child("select").toString();
    }

    @Override
    public Long getMaxAgeSeconds( Auth auth ) {
        return null;
    }

    @Override
    public String getName() {
        return "new";
    }

    @Override
    public Object authenticate( String user, String password ) {
        return folder.authenticate( user, password );
    }

    @Override
    public Object authenticate( DigestResponse digestRequest ) {
        return folder.authenticate( digestRequest );
    }

    @Override
    public boolean authorise( Request request, Request.Method method, Auth auth ) {
        ITemplate t = getTemplate( request.getParams() );
        Role creatingRole = _( AuthoringPermissionService.class ).getCreateRole( folder, t );
        boolean b = _( PermissionChecker.class ).hasRole( creatingRole, folder, auth );
        return b;
    }

    @Override
    public String getRealm() {
        return folder.getRealm();
    }

    @Override
    public Date getModifiedDate() {
        return null;
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public String getContentType( String accepts ) {
        return "text/html";
    }

    @Override
    public String checkRedirect( Request request ) {
        String t = templateName(request.getParams());
        if( t == null || t.length() == 0) {
            List<Template> allowedTemplates = folder.getAllowedTemplates();
            if( allowedTemplates != null && allowedTemplates.size() == 1 ) {
                String templateName = allowedTemplates.get( 0 ).getName();
                log.debug( "no template selected, and only one allowed, so auto-select it");
                return request.getAbsoluteUrl() + "?templateSelect=" + templateName;
            }
        }

        return null;
    }

    @Override
    public String processForm( Map<String, String> parameters, Map<String, FileItem> files ) throws BadRequestException, NotAuthorizedException, ConflictException {
        log.debug( "processForm" );
        ITemplate template = getTemplate( parameters );
        if( template == null ) {
            log.error( "didnt locate template" );
            return null;
        }
        String nameToUse = newName;
        if( newName.equals( AUTO_NAME ) ) {
            nameToUse = ClydeUtils.getDateAsNameUnique( this.folder );
        }
        editee = template.createPageFromTemplate( folder, nameToUse );
        if( editee instanceof EditableResource ) {

            EditableResource er = (EditableResource) editee;
            String ret = er.getEditPage().processForm( parameters, files );
            if( ret != null ) {
                return ret;
            } else {
                return null; // probably invalid input
            }
        } else {
            log.debug( "..NOT editbale" );
            editee.save();
            return editee.getHref();
        }
    }

    public Resource getEditPage() {
        return this;
    }

    @Override
    public void loadFromXml( Element el, Map<String, String> parameters ) {
        ITemplate template = getTemplate( parameters );
        editee = (Page) template.createPageFromTemplate( folder, newName );
        editee.loadFromXml( el );
    }

    @Override
    public Element toXml( Element el, Map<String, String> parameters ) {
        ITemplate template = getTemplate( parameters );
        editee = (Page) template.createPageFromTemplate( folder, newName );
        return editee.toXml( el );
    }

    @Override
    public void save() {
        editee.save();
    }
}
