package com.bradmcevoy.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.Templatable;
import java.util.Map;
import java.util.UUID;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class RelationSelectDef extends CommonComponent implements ComponentDef, Addressable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( TextDef.class );
    private static final long serialVersionUID = 1L;
    protected Addressable container;
    protected String name;
    protected String validationMessage;
    private boolean required;
    private String description;
    private String relationName;
    protected String selectFromFolder;
    protected String selectTemplate;

    public RelationSelectDef( Addressable container, String name ) {
        this.container = container;
        this.name = name;
    }

    public RelationSelectDef( Addressable container, Element el ) {
        this.container = container;
        this.name = el.getAttributeValue( "name" );
        required = InitUtils.getBoolean( el, "required" );
        description = InitUtils.getValue( el, "description" );
        relationName = InitUtils.getValue( el, "relationName" );
        selectFromFolder = InitUtils.getValue( el, "selectFromFolder" );
        selectTemplate = InitUtils.getValue( el, "selectTemplate" );
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
        return true;
    }

    @Override
    public boolean validate( RenderContext rc ) {
        if( name == null || name.trim().length() == 0 ) {
            validationMessage = "Please enter a name";
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Element toXml( Addressable container, Element el ) {
        Element e2 = new Element( "componentDef" );
        el.addContent( e2 );
        e2.setAttribute( "class", getClass().getName() );
        e2.setAttribute( "name", getName() );
        InitUtils.setBoolean( e2, "required", required );
        InitUtils.setString( e2, "description", description );
        InitUtils.setString( e2, "relationName", relationName );
        InitUtils.setString( e2, "selectFromFolder", selectFromFolder );
        InitUtils.setString( e2, "selectTemplate", selectTemplate );
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
        ComponentValue cv = new ComponentValue( name, "" );
        return cv;
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
        return "";
    }

    @Override
    public String render( ComponentValue c, RenderContext rc ) {
        BaseResource res = (BaseResource) c.getContainer();
        BaseResource related = res.getRelation( relationName );
        return related.getLink();
    }

    @Override
    public String renderEdit( ComponentValue c, RenderContext rc ) {
        log.debug( "renderEdit" );
        Templatable selectFrom = rc.getTarget().find( selectFromFolder );
        if( selectFrom == null ) {
            return "Error: couldnt find folder: " + selectFromFolder;
        } else if( selectFrom instanceof Folder ) {
            BaseResource page = (BaseResource) c.getContainer();
            BaseResource dest = page.getRelation( relationName );
            StringBuffer sb = new StringBuffer();
            sb.append( "<select name='" ).append( name ).append( "'>" );
            Folder fSelectFrom = (Folder) selectFrom;
            log.debug( "selectFrom: " + fSelectFrom.getHref() + " - " + selectTemplate );
            for( Templatable ct : fSelectFrom.getChildren( selectTemplate ) ) {
                log.debug( "..found: " + ct.getName() );
                if( ct instanceof BaseResource ) {
                    BaseResource res = (BaseResource) ct;
                    String sel = "";
                    if( dest != null && dest == res ) sel = " selected ";
                    sb.append( "<option value='" ).append( res.getNameNodeId().toString() ).append( "'" ).append( sel ).append( ">" ).append( res.getTitle() ).append( "</option>" );
                }
            }
            sb.append( "</select>" );
            return sb.toString();
        } else {
            return "Error: not a folder: " + selectFromFolder;
        }
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
        return UUID.fromString( s );
    }

    @Override
    public Class getValueClass() {
        return UUID.class;
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
        UUID id = (UUID) parseValue( componentValue, rc.page, s );
//        BaseResource dest =
        BaseResource res = (BaseResource) componentValue.getContainer();
        BaseResource dest = res.findByNameNodeId( id );
        res.createRelationship( relationName, dest );
    }

    @Override
    public void changedValue( ComponentValue cv ) {
        // big whoop
    }
}
