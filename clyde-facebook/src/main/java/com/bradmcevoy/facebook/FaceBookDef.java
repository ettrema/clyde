package com.bradmcevoy.facebook;

import com.bradmcevoy.context.RequestContext;
import com.bradmcevoy.facebook.FaceBookSessionValue.FaceBookSession;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ImageFile;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.InitUtils;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.jdom.CDATA;
import org.jdom.Element;
import org.mvel.TemplateInterpreter;

/**
 *
 * @author brad
 */
public class FaceBookDef implements ComponentDef, Serializable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( FaceBookDef.class );
    private static final long serialVersionUID = 1L;
    private String name;
    private String apiKey;
    private String apiSecret;
    private String loginTemplate;
    private Addressable container;

    public static FaceBookSession parse( String s ) {
        if( s != null && s.trim().length() > 0 ) {
            String[] arr = s.split( ":" );
            if( arr.length == 3 ) {
                Long userId = Long.parseLong( arr[2] );
                FaceBookSessionValue.FaceBookSession sess = new FaceBookSessionValue.FaceBookSession( arr[0], arr[1], userId );
                return sess;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


    public FaceBookDef( Addressable container, String name ) {
        this.container = container;
        this.name = name;
    }

    public FaceBookDef( Addressable container, Element el ) {
        this.container = container;
        this.name = el.getAttributeValue( "name" );
        this.apiKey = InitUtils.getValue( el, "apiKey" );
        this.apiSecret = InitUtils.getValue( el, "apiSecret" );
        this.loginTemplate = el.getChildTextTrim( "loginTemplate" );
    }

    public Element toXml( Addressable container, Element el ) {
        Element e2 = new Element( "componentDef" );
        el.addContent( e2 );
        e2.setAttribute( "class", getClass().getName() );
        e2.setAttribute( "name", getName() );
        InitUtils.setString( e2, "apiKey", apiKey );
        InitUtils.setString( e2, "apiSecret", apiSecret );
        Element elLogin = new Element( "loginTemplate" );
        if( !StringUtils.isEmpty( loginTemplate ) ) {
            elLogin.setContent( new CDATA( loginTemplate ) );
        }
        e2.addContent( elLogin );
        return e2;
    }

    public boolean validate( ComponentValue c, RenderContext rc ) {
        return true;
    }

    public String getName() {
        return name;
    }

    public String render( ComponentValue c, RenderContext rc ) {
        return "sessionId: " + c.getValue();
    }

    public String renderEdit( ComponentValue c, RenderContext rc ) {
        return "";
    }

    public void onPreProcess( ComponentValue componentValue, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
    }

    public ComponentValue createComponentValue( Templatable newRes ) {
        FaceBookSessionValue cv = new FaceBookSessionValue( name, newRes );
        return cv;
    }

    public Object parseValue( ComponentValue cv, Templatable ct, String s ) {
        return parse(s);
    }

    public Class getValueClass() {
        return FaceBookSession.class;
    }



    public String formatValue( Object v ) {
        if( v == null ) return "";
        if( v instanceof FaceBookSessionValue.FaceBookSession ) {
            FaceBookSessionValue.FaceBookSession sess = (FaceBookSession) v;
            return sess.getSessionId() + ":" + sess.getSecretId() + ":" + sess.getUserId().toString();
        } else {
            return v.toString();
        }
    }

    public void changedValue( ComponentValue cv ) {
    }

    public void init( Addressable container ) {
        this.container = container;
    }

    public Addressable getContainer() {
        return container;
    }

    public boolean validate( RenderContext rc ) {
        return true;
    }

    public String render( RenderContext rc ) {
        return "";
    }

    public String renderEdit( RenderContext rc ) {
        return "";
    }

    public String onProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        return null;
    }

    public void onPreProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
    }

    public void uploadPhotos( Folder parentFolder, FaceBookSession session, String caption ) {
        FaceBookGalleryManager faceBookGalleryManager = requestContext().get( FaceBookGalleryManager.class );
        if( faceBookGalleryManager == null ) {
            throw new RuntimeException( "No FaceBookGalleryManager is configured" );
        }
        FaceBookCredentials cred = new FaceBookCredentials( apiKey, apiSecret, session.getSecretId(), session.getSessionId(), session.getUserId() );
        Long albumId = faceBookGalleryManager.checkOrCreateAlbum( parentFolder.getName(), cred );
        for( Resource r : parentFolder.getChildren() ) {
            if( r instanceof ImageFile ) {
                ImageFile img = (ImageFile) r;
                faceBookGalleryManager.loadImageToAlbum( img, albumId, cred, caption );
            }
        }
    }

    public void uploadPhoto( ImageFile img, String albumName, String caption, FaceBookSession session ) {
        FaceBookGalleryManager faceBookGalleryManager = requestContext().get( FaceBookGalleryManager.class );
        if( faceBookGalleryManager == null ) {
            throw new RuntimeException( "No FaceBookGalleryManager is configured" );
        }
        FaceBookCredentials cred = new FaceBookCredentials( apiKey, apiSecret, session.getSecretId(), session.getSessionId(), session.getUserId() );
        Long albumId = faceBookGalleryManager.checkOrCreateAlbum( albumName, cred );
        faceBookGalleryManager.loadImageToAlbum( img, albumId, cred, caption );
    }

    private RequestContext requestContext() {
        return RequestContext.getCurrent();
    }

    public String getLogin(Templatable targetPage) {
        Map map = new HashMap();
        map.put("page", targetPage);
        return TemplateInterpreter.evalToString( loginTemplate, this, map );
    }

    public String getApiKey() {
        return apiKey;
    }

    
}
