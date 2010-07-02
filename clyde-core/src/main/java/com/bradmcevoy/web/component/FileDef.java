package com.bradmcevoy.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.Templatable;
import java.io.IOException;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.jdom.Element;

/**
 *
 */
public class FileDef extends CommonComponent implements ComponentDef, Addressable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( TextDef.class );
    private static final long serialVersionUID = 1L;
    protected Addressable container;
    private String name;
    private boolean required;
    private String description;

    public FileDef( Addressable container, String name ) {
        this.container = container;
        this.name = name;
    }

    public FileDef( Addressable container, Element el ) {
        this.container = container;
        this.name = el.getAttributeValue( "name" );
        required = InitUtils.getBoolean( el, "required" );
        description = InitUtils.getValue( el, "description" );
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
        log.debug("validate: " + name);
        if( required ) {
            if( c.getValue() == null ) {
                // Note this will be null if an error occurred in pre-processing
                // but he message will have been set
                if( c.getValidationMessage() == null ) {
                    c.setValidationMessage("A value is required");
                } else {
                    log.debug("not valid, message already set");
                }
                return false;
            }
        }
        log.debug("validation ok");
        return true;
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
        InitUtils.setBoolean( e2, "required", required );
        InitUtils.setString( e2, "description", description );
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
        cv.setValue(null);
        return cv;
    }


    protected String editChildTemplate() {
        String template = "<input type='file' name='${path}' value='${value}' size='${def.cols}' />";
        template = template + "#if($cv.validationMessage)";
        template = template + "<font color='red'>${cv.validationMessage}</font>";
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
        return "not supported: " + name;
    }

    @Override
    public String render( ComponentValue c, RenderContext rc ) {
        return "";
    }

    @Override
    public String renderEdit( ComponentValue c, RenderContext rc ) {
        String t = editChildTemplate();
        VelocityContext vc = velocityContext( rc, c );
        return _render( t, vc );
    }

    protected VelocityContext velocityContext( RenderContext rc, ComponentValue c ) {
        VelocityContext vc = velocityContext( rc, c.getValue() );
        EditSource es = new EditSource( c, rc );
        vc.put( "val", es );
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
        return s;
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
        FileItem fitem = files.get(this.getName());
        if( fitem != null ) {            
            if( componentValue.getContainer() instanceof Templatable ) {                
                Templatable res = (Templatable) componentValue.getContainer();
                if( res instanceof Folder ) {
                    Folder f = (Folder) res;
                    try {
                        f.createNew_notx(fitem.getName(), fitem.getInputStream(), fitem.getSize(), fitem.getContentType());
                        componentValue.setValue( fitem );
                    } catch (IOException ex) {
                        throw new RuntimeException("Couldnt read file",ex);
                    } catch (ConflictException ex) {
                        log.warn("Conflict exception writing uploaded file", ex);
                        componentValue.setValidationMessage("You dont have permission to write to this location");
                    }
                } else {
                    componentValue.setValidationMessage("Can't save the file because the new resource isnt a folder");
                }
            } else {
                componentValue.setValidationMessage("Can't save the file because the container isnt a Templatable type");
            }
        }
    }

    public class EditSource {

        ComponentValue c;
        RenderContext rc;

        EditSource( ComponentValue c, RenderContext rc ) {
            this.c = c;
            this.rc = rc;
        }

        public Object getValue() {
            return c.getValue();
        }

        public String getFormattedValue() {
            return formatValue( c.getValue() );
        }

        public Path getPath() {
            Path p = FileDef.this.getPath( rc );
            return p;
        }

        public Object getDef() {
            return FileDef.this;
        }
    }

    @Override
    public void changedValue( ComponentValue cv ) {
        // big whoop
    }
}
