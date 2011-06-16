package com.bradmcevoy.facebook;

import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.ImageFile;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import java.io.Serializable;
import org.jdom.Element;


/**
 *
 * @author brad
 */
public class FaceBookSessionValue extends ComponentValue {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( FaceBookSessionValue.class );
    private static final long serialVersionUID = 1L;

    public FaceBookSessionValue( String name, Addressable container ) {
        super( name, container );
    }

    public FaceBookSessionValue( Element el, Templatable container ) {
        super( el, container );
    }

    @Override
    public FaceBookSession getValue() {
        Object o = super.getValue();
        if( o == null ) {
            return null;
        } else if( o instanceof FaceBookSession ) {
            return (FaceBookSession) o;
        } else if( o instanceof String ) {
            String s = (String) o;
            if( s.length() > 0 ) {
                return FaceBookDef.parse( s );
            } else {
                return null;
            }
        } else {
            log.warn("Invalid value type: " + o.getClass());
            return null;
        }
    }

    @Override
    public void setValue( Object value ) {
        if( value instanceof String) {
            String sValue = (String) value;
            super.setValue( FaceBookDef.parse( sValue) );
        } else {
            super.setValue( value );
        }
    }



    public void setValue( String sessionId, String sessionSecret, Long userId ) {
        log.debug("setValue: " + sessionId + " " + sessionSecret + " " + userId);
        FaceBookSession sess = new FaceBookSession( sessionId, sessionSecret, userId );
        this.setValue( sess );
    }

    public void setValue( String sessionId, String sessionSecret, Integer userId ) {
        log.debug("setValue: " + sessionId + " " + sessionSecret + " " + userId);
        FaceBookSession sess = new FaceBookSession( sessionId, sessionSecret, userId.longValue() );
        this.setValue( sess );
    }

    public void uploadPhotos( Folder folder, String caption ) {
        FaceBookSession session = this.getValue();
        if( session == null ) {
            throw new RuntimeException( "not logged in" );
        }
        Templatable page = (Templatable) this.getContainer();
        FaceBookDef def = (FaceBookDef) page.getTemplate().getComponentDef( name );
        def.uploadPhotos( folder, session, caption );
    }


    public void uploadPhoto( ImageFile img, String albumName, String caption ) {
        FaceBookSession session = this.getValue();
        if( session == null ) {
            throw new RuntimeException( "not logged in" );
        }
        if( this.getContainer() == null) {
            throw new RuntimeException( "container is null. The parent page probably needs resaving");
        }
        Templatable page = (Templatable) this.getContainer();
        ITemplate template = page.getTemplate();
        if(template == null) throw new RuntimeException( "No template for: " + page.getHref());
        FaceBookDef def = (FaceBookDef) template.getComponentDef( name );
        if( def == null ) throw new RuntimeException( "No FaceBookDef for value: " + name);
        def.uploadPhoto( img, albumName, caption, session );
    }

    public String getLogin() {
        Templatable page = (Templatable) this.getContainer();
        if(page == null ) throw new RuntimeException( "No container for this value");
        ComponentDef def = this.getDef( (Templatable) this.getContainer() );
        if( def == null) throw new RuntimeException( "No template or definition");
        FaceBookDef fbDef = (FaceBookDef) def;
        return fbDef.getLogin(page);
    }

    public static class FaceBookSession implements Serializable {
        private static final long serialVersionUID = -229426431884516164L;
        private String sessionId;
        private String secretId;
        private Long userId;

        public FaceBookSession( String sessionId, String secretId, Long userId ) {
            this.sessionId = sessionId;
            this.secretId = secretId;
            this.userId = userId;
        }


        public String getSecretId() {
            return secretId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSecretId( String secretId ) {
            this.secretId = secretId;
        }

        public void setSessionId( String sessionId ) {
            this.sessionId = sessionId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId( Long userId ) {
            this.userId = userId;
        }
    }
}
