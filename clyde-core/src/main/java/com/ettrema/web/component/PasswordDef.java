package com.ettrema.web.component;

import com.bradmcevoy.http.Request;
import com.ettrema.web.User;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.HttpManager;
import com.ettrema.web.BaseResource;
import com.ettrema.web.RenderContext;
import com.ettrema.web.Templatable;
import com.ettrema.web.security.PasswordValidationService;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;

/**
 * 
 * Not finished
 *
 * @author brad
 */
public class PasswordDef extends CommonComponent implements ComponentDef, Addressable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( PasswordDef.class );
    private static final long serialVersionUID = 1L;
    private static final String HIDDEN_PASSWORD = "********";
    protected Addressable container;
    private String name;

    public PasswordDef( Addressable container, String name ) {
        this.container = container;
        this.name = name;
    }

    public PasswordDef( Addressable container, Element el ) {
        this.container = container;
        this.name = el.getAttributeValue( "name" );
    }

    @Override
    public void init( Addressable container ) {
        this.container = container;
    }

    @Override
    public Addressable getContainer() {
        return this.container;
    }

    @Override
    public boolean validate( ComponentValue c, RenderContext rc ) {
        String s;
        Object val = c.getValue();
        if( val instanceof String ) {
            s = (String) val;
        } else if( val == null ) {
            s = null;
        } else {
            s = val.toString();
        }
        if( s == null || s.trim().length() == 0 ) {
            c.setValidationMessage( "A value is required" );
            return false;
        }
        User user = (User) c.getContainer();
        String err = _( PasswordValidationService.class ).checkValidity( user, s );
        if( err == null ) {
            log.trace( "password is ok" );
            return true;
        } else {
            log.info( "password invalid: " + err );
            c.setValidationMessage( err );
            return false;
        }
    }

    @Override
    public boolean validate( RenderContext rc ) {
        return true;
    }

    @Override
    public Element toXml( Addressable container, Element el ) {
        Element e2 = new Element( "componentDef" );
        el.addContent( e2 );
        e2.setAttribute( "class", getClass().getName() );
        e2.setAttribute( "name", getName() );
        return e2;
    }

    @Override
    public Path getPath() {
        return container.getPath().child( name );
    }

    /**
     *
     * @param newPage
     * @return - create an empty intance of a value containing object suitable
     * for this def
     */
    @Override
    public ComponentValue createComponentValue( Templatable newPage ) {
        ComponentValue cv = new ComponentValue( name, newPage );
        cv.init( newPage );
        cv.setValue( "" );
        return cv;
    }

    protected String editChildTemplate( Map<String, String> parameters, boolean isNew ) {
        String displayValue = isNew ? "" : HIDDEN_PASSWORD;
        if( parameters != null && parameters.containsKey( getName() ) ) {
            displayValue = parameters.get( getName() );
        }
        String template = "";
        template = "<input type='password' name='${path}' value=\"" + displayValue + "\" />";
        template = template + "#if($cv.validationMessage)";
        template = template + "<div class='validationError'>${cv.validationMessage}</div>";
        template = template + "#end";
        return template;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String render( RenderContext rc ) {
        return renderEdit( rc );
    }

    @Override
    public String renderEdit( RenderContext rc ) {
        return ""; // TODO
    }

    @Override
    public String render( ComponentValue c, RenderContext rc ) {
        return "********";
    }

    @Override
    public String renderEdit( ComponentValue c, RenderContext rc ) {
        //log.debug("renderEdit: " + c.getName() + " - " + c.getValidationMessage());
        Map<String, String> params = null;
        Request request = HttpManager.request();
        if( request != null ) {
            params = request.getParams();
        }
        String t = editChildTemplate( params, isNew(c) );
        VelocityContext vc = velocityContext( rc, c );
        return _render( t, vc );
    }

    /**
     * true if the container is a new object.
     *
     * @param cv
     * @return
     */
    private boolean isNew(ComponentValue cv) {
       if( cv.getContainer() instanceof BaseResource ) {
           return ((BaseResource)cv.getContainer()).isNew();
       } else {
           return false;
       }
    }

    protected VelocityContext velocityContext( RenderContext rc, ComponentValue c ) {
        VelocityContext vc = velocityContext( rc, c.getValue() );
        vc.put( "cv", c );
        return vc;
    }

    @Override
    public void onPreProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
    }

    @Override
    public String onProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        return null;
    }

    @Override
    public Object parseValue( ComponentValue cv, Templatable ct, String s ) {
        if( HIDDEN_PASSWORD.equals( s ) ) {
            log.trace( "password value is unchanged" );
            return cv.getValue();
        } else {
            log.trace( "password has changed" );
            return s;
        }
    }

    public Object parseValue(ComponentValue cv, Templatable ct, Element elValue) {
        String sVal = InitUtils.getValue( elValue );
        return sVal;
    }



    @Override
    public Class getValueClass() {
        return String.class;
    }

    @Override
    public String formatValue( Object v ) {
        if( v == null ) {
            return "";
        }
        return v.toString();
    }

    /** Do pre-processing for child component. This means that it will parse the request
     *  parameter and set the value on the child
     */
    @Override
    public void onPreProcess( ComponentValue componentValue, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        Path compPath = getPath( rc );
        String key = compPath.toString();
        if( !parameters.containsKey( key ) ) {
            return;
        }
        String s = parameters.get( key );
        Object value = parseValue( componentValue, rc.page, s );
        componentValue.setValue( value );
    }

    public void changedValue( ComponentValue cv ) {
    }
}
