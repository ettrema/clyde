package com.bradmcevoy.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.utils.IntegerUtils;
import com.bradmcevoy.web.*;
import java.util.List;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.jdom.Element;

public class TextDef extends CommonComponent implements ComponentDef, Addressable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( TextDef.class );
    private static final long serialVersionUID = 1L;
    protected Addressable container;
    protected final Text name = new Text( this, "name" );
    NumberInput rows = new NumberInput( this, "rows" );
    NumberInput cols = new NumberInput( this, "cols" );
    private boolean required;
    private List<String> choices;
    private String description;
    private boolean disAllowTemplating;

    public TextDef( Addressable container, String name ) {
        this.container = container;
        this.name.setValue( name );
    }

    public TextDef( Addressable container, String name, int cols, int rows ) {
        this( container, name );
        this.rows.setValue( rows );
        this.cols.setValue( cols );
    }

    public TextDef( Addressable container, Element el ) {
        this.container = container;
        this.name.setValue( el.getAttributeValue( "name" ) );
        Integer r = IntegerUtils.parseInteger( el.getAttributeValue( "rows" ) );
        this.rows.setValue( r );
        Integer c = IntegerUtils.parseInteger( el.getAttributeValue( "cols" ) );
        this.cols.setValue( c );
        required = InitUtils.getBoolean( el, "required" );
        choices = InitUtils.getList( el, "choices" );
        description = InitUtils.getValue( el, "description" );
        disAllowTemplating = InitUtils.getBoolean( el, "disAllowTemplating" );
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
        if( required ) {
            if( s == null || s.trim().length() == 0 ) {
                c.setValidationMessage( "A value is required" );
                return false;
            }
        }
        if( choices != null && choices.size() > 0 ) {
            if( !choices.contains( s ) ) {
                String err = "The value must be one of: ";
                for( String ch : choices ) {
                    err += ch + ",";
                }
                c.setValidationMessage( err );
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean validate( RenderContext rc ) {
        return true;
    }

    public NumberInput getRows() {
        return rows;
    }

    public NumberInput getCols() {
        return cols;
    }

    public Integer getRowsVal() {
        if( rows == null ) {
            return null;
        }
        return rows.getValue();
    }

    public void setRowsVal( Integer i ) {
        if( rows == null ) {
            rows = new NumberInput( container, "rows" );
        }
        rows.setValue( i );
    }

    public Integer getColsVal() {
        if( cols == null ) {
            return null;
        }
        return cols.getValue();
    }

    public void setColsVal( Integer i ) {
        if( rows == null ) {
            cols = new NumberInput( container, "cols" );
        }
        cols.setValue( i );
    }

    @Override
    public Element toXml( Addressable container, Element el ) {
        Element e2 = new Element( "componentDef" );
        el.addContent( e2 );
        e2.setAttribute( "class", getClass().getName() );
        e2.setAttribute( "name", getName() );
        e2.setAttribute( "rows", rows == null ? "" : rows.toString() );
        e2.setAttribute( "cols", cols == null ? "" : cols.toString() );
        InitUtils.setBoolean( e2, "required", required );
        InitUtils.setList( e2, "choices", choices );
        InitUtils.setString( e2, "description", description );
        InitUtils.setBoolean( e2, "disAllowTemplating", disAllowTemplating );
        return e2;
    }

    @Override
    public Path getPath() {
        return container.getPath().child( name.getValue() );
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired( boolean required ) {
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public boolean isDisAllowTemplating() {
        return disAllowTemplating;
    }

    public void setDisAllowTemplating( boolean disAllowTemplating ) {
        this.disAllowTemplating = disAllowTemplating;
    }

    /**
     * 
     * @param newPage
     * @return - create an empty intance of a value containing object suitable
     * for this def
     */
    @Override
    public ComponentValue createComponentValue( Templatable newPage ) {
        ComponentValue cv = new ComponentValue( name.getValue(), newPage );
        cv.init( newPage );
        cv.setValue( "" );
        return cv;
    }

    public List<String> getChoices() {
        return choices;
    }

    public void setChoices(List<String> choices) {
        this.choices = choices;
    }
    

    protected String editChildTemplate() {
        String template = "";
        String reqClass = required ? "class='required'" : "";
        if( choices != null && choices.size() > 0 ) {
            StringBuilder sb = new StringBuilder( "<select id='${path}' name='${path}' " + reqClass + " >" );
            sb.append( "#foreach($choice in $def.choices)" );
            sb.append( "<option #if($value==$choice)selected #end>$choice</option>" );
            sb.append( "#end" );
            sb.append( "</select>" );
            template = sb.toString();
        } else {
            if( rows == null || rows.getValue() == null || rows.getValue() == 1 ) {
                template = "<input type='text' name='${path}' " + reqClass + " value=\"${val.formattedValue}\" size='${def.cols}' id='${path}' />";
            } else {
                template = "<textarea id='${path}' name='${path}' " + reqClass + " rows='${def.rows}' cols='${def.cols}'>${val.formattedValue}</textarea>";
                //template = "<div id='${path}' class='editDiv' onclick='enableEdit(this)'>${value}</div>";
            }
        }
        template = template + "#if($cv.validationMessage)";
        template = template + "<div class='validationError'>${cv.validationMessage}</div>";
        template = template + "#end";
//        template = "<acronym title='${path}'>" + template + "</acronym>";
        return template;
    }

    @Override
    public String getName() {
        return name.getValue();
    }

    @Override
    public String render( RenderContext rc ) {
        return renderEdit( rc );
    }

    @Override
    public String renderEdit( RenderContext rc ) {
        return "name: " + name.renderEdit( rc );
    }

    @Override
    public String render( ComponentValue c, RenderContext rc ) {
        if( c.getValue() == null ) {
            return "";
        }
        String template = c.getValue().toString();
        if( log.isTraceEnabled() ) {
            log.trace( "template: " + template );
        }
        if( !disAllowTemplating ) {
            log.trace( "merge template" );
            VelocityContext vc = velocityContext( rc, c );
            return _render( template, vc );
        } else {
            log.trace( "templating disallowed" );
            return template;
        }
        //return eval(template, rc);
    }

    @Override
    public String renderEdit( ComponentValue c, RenderContext rc ) {
        //log.debug("renderEdit: " + c.getName() + " - " + c.getValidationMessage());
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
        name.onPreProcess( rc, parameters, files );
        if( rows != null ) {
            rows.onPreProcess( rc, parameters, files );
        }
        if( cols != null ) {
            cols.onPreProcess( rc, parameters, files );
        }
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
        return ComponentUtils.encodeHTML( v.toString() );
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
            Path p = TextDef.this.getPath( rc );
            return p;
        }

        public String getName() {
            return TextDef.this.getName();
        }

        public Object getDef() {
            return TextDef.this;
        }

        public boolean isRequired() {
            return TextDef.this.required;
        }

        public String getChecked() {
            if( c.getValue() != null ) {
                if( BooleanDef.parse( c ) ) {
                    return " checked='true' ";
                } else {
                    return "";
                }
            } else {
                return "";
            }
        }
    }

    @Override
    public void changedValue( ComponentValue cv ) {
        // big whoop
    }
}
