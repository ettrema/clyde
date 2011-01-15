package com.bradmcevoy.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.ExistingResourceFactory;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.Templatable;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

/**
 * Accepts an input value which is either a name of a resource in the selectFrom
 * folder or a UUID of a resource
 *
 * But will always set a value of the UUID
 *
 * @author brad
 */
public class RelationSelectDef extends CommonComponent implements ComponentDef, Addressable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( RelationSelectDef.class );
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
        Object val = c.getValue();
        log.trace("validate: " + val);
        if( required ) {
            if( ComponentUtils.isEmpty( val ) ) {
                log.trace( "required, and no value given" );
                c.setValidationMessage( "Please enter a value" );
                return false;
            } else {
                if( log.isTraceEnabled() ) {
                    log.trace( "required and value supplied, so ok: " + val );
                }
            }
        } else {
            if( ComponentUtils.isEmpty( val ) ) {
                log.trace( "not required and empty so apply no validation" );
                return true;
            }
        }
        Resource r = findResource( val, rc.getTargetPage() );
        if( r == null ) {
            c.setValidationMessage( "Invalid value" );
            log.trace( "not valid because no resource was found" );
            return false;
        } else {
            return true;
        }

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
        ComponentValue cv = new ComponentValue( name, newPage );
        cv.init( newPage );
        cv.setValue( "" );
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
        if( related != null ) {
            return related.getLink();
        } else {
            return "";
        }
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
            StringBuilder sb = new StringBuilder();
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
        if( StringUtils.isNotBlank( s ) ) {
            try {
                return UUID.fromString( s );
            } catch( IllegalArgumentException e ) {
                BaseResource res = (BaseResource) findResource( s, ct );
                if( res != null ) {
                    return res.getNameNodeId();
                } else {
                    log.warn( "not found: " + s );
                    // return the invalid value so it can be used in validation. Not that
                    // efficient, should set some temporary value
                    return s;
                }
            }
        } else {
            return null;
        }
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
        Object value = parseValue( componentValue, rc.page, s );
        if( value != null && !(value instanceof UUID) ) {
            log.trace("not a valid uuid, so dont do anything");
            componentValue.setValue( value );
            return ;
        }
        UUID id = (UUID)value;
//        BaseResource dest =
        BaseResource res = (BaseResource) componentValue.getContainer();
        boolean found = false;
        BaseResource existingBaseRes = res.getRelation( relationName );
        if( existingBaseRes != null ) {
            if( !existingBaseRes.getNameNodeId().equals( id ) ) {
                // same relationship to somewhere else, so remove it
                if( log.isDebugEnabled() ) {
                    log.debug( "remove relationship: " + relationName + " from: " + existingBaseRes.getHref() );
                }
                res.removeRelationship( relationName );
            } else {
                // already exists, do nothing
                found = true;
            }
        } else {
            if( id == null ) {
                log.trace( "selected value is null, and no current value. So do nothing" );
                found = true;
            } else {
                log.trace( "current relationship doesnt exist, but value is selected. Create." );
            }
        }
        if( !found ) {
            BaseResource dest = res.findByNameNodeId( id );
            if( log.isDebugEnabled() ) {
                log.debug( "create relationship: " + relationName + " to: " + dest.getHref() );
            }
            res.createRelationship( relationName, dest );
            componentValue.setValue( id );
        }
    }

    @Override
    public void changedValue( ComponentValue cv ) {
        // big whoop
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

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName( String relationName ) {
        this.relationName = relationName;
    }

    public String getSelectFromFolder() {
        return selectFromFolder;
    }

    public void setSelectFromFolder( String selectFromFolder ) {
        this.selectFromFolder = selectFromFolder;
    }

    public String getSelectTemplate() {
        return selectTemplate;
    }

    public void setSelectTemplate( String selectTemplate ) {
        this.selectTemplate = selectTemplate;
    }

    private Resource findResource( Object val, Templatable page ) {
        UUID id;
        if( val == null ) {
            return null;
        } else if( val instanceof String ) {
            String sVal = (String) val;
            try {
                id = UUID.fromString( sVal );
            } catch( IllegalArgumentException e ) {
                // Not a UUID, so look for name
                Folder selectFrom = (Folder)ComponentUtils.find( page, Path.path( selectFromFolder ) );
                Resource child = selectFrom.child( sVal );
                if( child == null ) {
                    log.trace( "no child found called: " + sVal );
                    return null;
                } else {
                    log.trace( "found child" );
                    return child;
                }
            }
        } else if( val instanceof UUID ) {
            id = (UUID) val;
        } else {
            log.warn( "unknown value type: " + val.getClass() );
            return null;
        }
        BaseResource res = ExistingResourceFactory.get( id );
        if( log.isTraceEnabled() ) {
            if( res == null ) {
                log.warn( "no resource found with id: " + id );
            } else {
                log.trace( "found resource with id: " + id );
            }
        }
        return res;
    }
}
