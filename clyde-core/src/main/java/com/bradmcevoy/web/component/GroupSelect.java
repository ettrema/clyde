package com.bradmcevoy.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.User;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

/**
 * Used to toggle group membership on and off
 *
 * @author brad
 */
public final class GroupSelect implements WrappableComponent, Component {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( GroupSelect.class );
    private static final long serialVersionUID = 1L;
    private Addressable container;
    private String name;
    private String groupName;

    public GroupSelect( Addressable container, String name ) {
        this.container = container;
        this.name = name;
    }

    public GroupSelect( Addressable container, Element el ) {
        this.container = container;
        groupName = InitUtils.getValue( el, "group" );
        name = InitUtils.getValue( el, "name" );
    }

    @Override
    public void init( Addressable container ) {
        if( container == null )
            throw new IllegalArgumentException( "container is null" );
        this.container = container;
    }

    @Override
    public Addressable getContainer() {
        return container;
    }

    @Override
    public Element toXml( Addressable container, Element el ) {
        Element e2 = new Element( "component" );
        el.addContent( e2 );
        String cName = name;
        if( StringUtils.isEmpty( name )) {
            log.warn("empty name for: " + groupName);
            cName = "null_name";
        }
        e2.setAttribute( "name", cName );
        InitUtils.set( e2, "group", groupName );
        e2.setAttribute( "class", this.getClass().getName() );
        return e2;
    }

    @Override
    public String render( RenderContext rc ) {
        return "";
    }

    @Override
    public String renderEdit( RenderContext child ) {
        return renderEdit( child.getTargetPage(), child );
    }

    @Override
    public void onPreProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        onPreProcess( rc.getTargetPage(), rc, parameters, files );
    }

    @Override
    public String onProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        return null;

    }

    @Override
    public String getName() {
        return name;
    }

    public Path getPath( RenderContext rc ) {
        return Path.path( name );
    }

    protected Boolean parse( String s ) {
        return Boolean.parseBoolean( s );
    }

    public void onPreProcess( Addressable container, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        try {
            String paramName = getPath( rc ).toString();
            // Note that then path is the name of the dummy element which is always present
            // The actual value, which is only posted if checked, is path + "Val"
            String dummyName = paramName;
            if( !parameters.containsKey( dummyName ) ) return;
            String s = parameters.get( paramName + "Val" );
            Boolean val;
            if( s != null ) {
                val = parse( s );
            } else {
                val = false;
            }

            User user = (User) container;
//            if( user.isNew()) {
//                user.save();
//            }
            if( val ) {
                user.addToGroup( groupName );
            } else {
                user.removeFromGroup( groupName );
            }
        } catch( Exception e ) {
            log.error( "group exception", e );
            rc.addAttribute( "validation" + getPath( rc ), e.getMessage() );
        }

    }

    public String onProcess( Addressable container, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {        
        return null;

    }

    public String render( Addressable container, RenderContext rc ) {
        return "";
    }

    public String renderEdit( Addressable container, RenderContext child ) {
        String path = getPath( child ).toString();
        String checked = this.getValue( child.page ) ? "checked='yes'" : "";
        String dummy = "<input type='hidden' name='" + path + "' value='1'/>";
        return "<input type='checkbox' name='" + path + "Val' value='true' " + checked + " />" + dummy;
    }

    public boolean validate( Addressable container, RenderContext rc ) {
        String attName = "validation" + getPath( rc );
        String validationMessage = (String) rc.getAttribute( attName);
        if( validationMessage != null ) {
            log.warn("validation error: " + validationMessage);
            return false;
        } else {
            return true;
        }
    }

    public Boolean getValue( Addressable container ) {
        User user = (User) container;
        return user.isInGroup( groupName );
    }

    public String getFormattedValue( Addressable container ) {
        return getValue( container ) + "";
    }

    public boolean validate( RenderContext rc ) {
        return validate(rc.page, rc);
    }

    public void setGroupName( String groupName ) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    
}
