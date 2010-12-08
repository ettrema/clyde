package com.bradmcevoy.web.ajax;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.ExistingResourceFactory;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;

/**
 * Supports invoking components via ajax, with response as JSON data
 *
 * @author brad
 */
public class AjaxResourceFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( AjaxResourceFactory.class );
    private static final String NAME = ".ajax";
    private static final String ENDS_WITH = "/" + NAME;
    private final ExistingResourceFactory wrapped;

    public AjaxResourceFactory( ExistingResourceFactory wrapped ) {
        this.wrapped = wrapped;
    }

    public Resource getResource( String host, String path ) {
        if( path.endsWith( ENDS_WITH ) ) {
            Path p = Path.path( path );
            if( p.getParent() == null ) {
                return null;
            }
            Path pRes = p.getParent();
            Resource res = wrapped.getResource( host, pRes.toString() );
            if( res == null ) {
                return null;
            } else if( res instanceof CommonTemplated ) {
                CommonTemplated ct = (CommonTemplated) res;
                return new AjaxPostResource( ct );
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public class AjaxPostResource implements PostableResource, DigestResource {

        private final CommonTemplated res;

        public AjaxPostResource( CommonTemplated res ) {
            this.res = res;
        }

        public String processForm( Map<String, String> parameters, Map<String, FileItem> files ) throws BadRequestException, NotAuthorizedException, ConflictException {
            log.trace( "processForm" );
            preProcess( null, parameters, files );
            String s = process( null, parameters, files );
            return s;

//            String command = parameters.get("command");
//            if( command == null ) {
//                log.trace("processForm: no command");
//                return null;
//            }
//            Component c = res.getComponent( command );
//            if( c == null ) {
//                log.trace("processForm: component not found: " + command);
//                return null;
//            }
//            RenderContext rc = new RenderContext( res.getTemplate(), res, null, true);
//
//            c.onPreProcess( rc, parameters, files );
//
        }

        public void preProcess( RenderContext rcChild, Map<String, String> parameters, Map<String, FileItem> files ) {
            ITemplate lTemplate = res.getTemplate();
            RenderContext rc = new RenderContext( lTemplate, res, rcChild, false );
            if( lTemplate != null ) {
                lTemplate.preProcess( rc, parameters, files );
                for( ComponentDef def : lTemplate.getComponentDefs().values() ) {
                    if( !res.getValues().containsKey( def.getName() ) ) {
                        ComponentValue cv = def.createComponentValue( res );
                        res.getValues().add( cv );
                    }
                }
            }

            for( String paramName : parameters.keySet() ) {
                Path path = Path.path( paramName );
                Component c = rc.findComponent( path );
                if( c != null ) {
                    c.onPreProcess( rc, parameters, files );
                }
            }
        }

        public String process( RenderContext rcChild, Map<String, String> parameters, Map<String, FileItem> files ) throws NotAuthorizedException {
            log.info( "process form" );
            ITemplate lTemplate = res.getTemplate();
            RenderContext rc = new RenderContext( lTemplate, res, rcChild, false );

            for( String paramName : parameters.keySet() ) {
                Path path = Path.path( paramName );
                Component c = rc.findComponent( path );
                if( c != null ) {
                    c.onProcess( rc, parameters, files );
                    // ignore redirects
                    break;
                }
            }

            return null;
        }

        public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
            Map<String, String> errors = new HashMap<String, String>();
            for( ComponentValue cv : res.getValues().values() ) {
                String v = cv.getValidationMessage();
                errors.put( cv.getName(), v );
            }
            JsonConfig cfg = new JsonConfig();
            cfg.setIgnoreTransientFields( true );
            cfg.setCycleDetectionStrategy( CycleDetectionStrategy.LENIENT );

            JSON json = JSONSerializer.toJSON( errors, cfg );
            Writer writer = new PrintWriter( out );
            json.write( writer );
            writer.flush();
        }

        public Long getMaxAgeSeconds( Auth auth ) {
            return null;
        }

        public String getContentType( String accepts ) {
            return "application/x-javascript; charset=utf-8";
        }

        public Long getContentLength() {
            return null;
        }

        public String getUniqueId() {
            return null;
        }

        public String getName() {
            return NAME;
        }

        public Object authenticate( String user, String password ) {
            return res.authenticate( user, password );
        }

        public boolean authorise( Request request, Method method, Auth auth ) {
            return res.authorise( request, method, auth );
        }

        public String getRealm() {
            return res.getRealm();
        }

        public Date getModifiedDate() {
            return null;
        }

        public String checkRedirect( Request request ) {
            return null;
        }

        public Object authenticate( DigestResponse digestRequest ) {
            return res.authenticate( digestRequest );
        }

        public boolean isDigestAllowed() {
            return res.isDigestAllowed();
        }
    }
}
