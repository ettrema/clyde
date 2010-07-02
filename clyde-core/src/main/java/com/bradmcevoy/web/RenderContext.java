package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.context.RequestContext;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.component.Command;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.security.PermissionChecker;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;

public class RenderContext implements Map<String, Component> {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( RenderContext.class );
    private static final ReplaceableHtmlParser PARSER = new ReplaceableHtmlParserImpl();

    /**
     * Build a path object for the given container. Implemented here because
     * it must be symmetrical with findComponent(Path)
     * 
     * @param container
     * @return - a path from the host to the container
     */
    public static Path findPath( Addressable container ) {
        Addressable parent = container.getContainer();
        Path parentPath;
        if( parent != null && !( parent instanceof Host ) ) {
            parentPath = findPath( parent );
        } else {
            parentPath = Path.root();
        }
        return parentPath.child( container.getName() );
    }

    public static Path findPath( Component c ) {
        Path p = findPath( c.getContainer() );
        return p.child( c.getName() );
    }

    public static Templatable find( Templatable from, Path p ) {
        Templatable ct;
        if( !p.isRelative() ) {
            ct = findPageWithRelativePath( p, from.getWeb() );
        } else {
            ct = findPageWithRelativePath( p, from );
        }
        return ct;
    }
    final public ITemplate template;
    final public Templatable page;
    final public RenderContext child;
    final public boolean editMode;
    final Map<String, Object> attributes = new HashMap<String, Object>();

    public RenderContext( ITemplate template, Templatable page, RenderContext child, boolean editMode ) {
        this.template = template;
        this.page = page;
        this.child = child;
        this.editMode = editMode;
    }

    public boolean hasRole( String s ) {
        PermissionChecker permissionChecker = RequestContext.getCurrent().get( PermissionChecker.class );
        Role r = Role.valueOf( s );
        return permissionChecker.hasRole( r, getTargetPage(), RequestParams.current().getAuth() );
    }

    public DateTime toJodaDate( Date dt ) {
        return new DateTime( dt.getTime() );
    }

    public void addAttribute( String key, Object val ) {
        attributes.put( key, val );
    }

    /**
     * gets an attribute by key
     * 
     * @param key
     * @return
     */
    public Object getAttribute( String key ) {
        Object o = attributes.get( key );
        if( o == null ) {
            log.warn( "not found: " + key + " size:" + attributes.size() );
        }
        return o;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String getFormStart() {
        RequestParams params = RequestParams.current();
        String url;
        if( params != null ) {
            url = params.href;
        } else {
            url = "";
        }
        String s = "<form method=\"post\" action=\"" + url + "\">";
        if( params != null && params.parameters != null && params.parameters.containsKey( NewPage.selectName() ) ) { // oh my god, this is horrible
            s = s + "<input type='hidden' name='" + NewPage.selectName() + "' value='" + params.parameters.get( NewPage.selectName() ) + "' />";
        }
        return s;
    }

    public String getFormEnd() {
        return "</form>";
    }

    public String templateResource( String name ) {
        Templatable target = this.getTargetPage();
        if( target instanceof Template ) {
            return name;
        } else {
            Folder templates = target.getWeb().getTemplates();
            BaseResource r = templates.childRes( name );
            if( r == null ) {
                log.warn( "Did not find template resource: " + name + " in folder: " + templates.getPath() );
                return templates.getHref() + name;
            } else {
                return r.getHref();
            }
        }
    }

    public Auth getAuth() {
        return RequestParams.current().auth;
    }

    public String getActualHref() {
        return RequestParams.current().href;
    }

    public RenderContext getChild() {
        return child;
    }

    public boolean getEditMode() {
        return editMode;
    }

    public Templatable getMe() {
        return page;
    }

    public RenderContext getTarget() {
        if( child == null ) {
            return this;
        } else {
            return child.getTarget();
        }
    }

    public Templatable getTargetPage() {
        if( child == null ) {
            return page;
        } else {
            return child.getTargetPage();
        }
    }

    public RenderContext getTargetContext() {
        if( child == null ) {
            return this;
        } else {
            return child.getTargetContext();
        }
    }

    public String doBody() {
        if( child == null ) return "";
        //      log.debug("doBody1: " + this.page.getName());
        String s = doBody( child );
        if( s == null ) s = "";
        return s;
    }

    /** Returns the rendered body component value for this page
     */
    public String doBody( RenderContext rcChild ) {
        //log.debug( "doBody: page: " + rcChild.page.getName());
        Templatable childPage = rcChild.page;
        ComponentValue cvBody = childPage.getValues().get( "body" );
        if( cvBody == null ) {
            cvBody = new ComponentValue( "body", childPage );
            cvBody.init( childPage );
            childPage.getValues().add( cvBody );
        }
        if( rcChild.editMode ) {
            //log.debug( "edit");
            return cvBody.renderEdit( rcChild );
        } else {
            //log.debug( "not edit: isTemplate" + (rcChild.page instanceof Template) + " - child is null: " + (rcChild.child == null));
            if( rcChild.child == null && rcChild.page instanceof Template ) {
                log.debug( "output source" );
                Object val = cvBody.getValue();
                if( val == null ) {
                    return "";
                } else {
                    return wrapWithIdentifier( val.toString(), "body" );
                }
            } else {
                String body = cvBody.render( rcChild );
                if( rcChild.child == null ) {
                    return wrapWithIdentifier( body, "body" );
                } else {
                    return body;
                }
            }
        }
    }

    public String invoke( String paramName, boolean editable ) {
        return invoke( paramName, editable, editable );
    }

    private String invoke( String paramName, boolean editable, boolean markers ) {
//        log.debug("invoke: " + paramName + " on " + this.page.getName());
        try {
            RenderContext childRc = this.child == null ? this : this.child;
            Path p = Path.path( paramName );
            // First, look for a component in this page
            Component c = findComponent( p, this.page );
            if( c == null ) {
                log.debug( "component not found: " + p + " in: " + page.getHref() );
                return "";
            }
//            log.debug("found component: " + c.getClass() + " - " + c.getName() + " from path: " + p);
            String s;
            if( c instanceof ComponentDef ) {
                ComponentDef def = (ComponentDef) c;
                Templatable targetPage = this.getTargetPage();
                ComponentValue cv = getComponentValue( paramName, targetPage );
                if( cv == null && editable && targetPage instanceof BaseResource ) {
                    cv = def.createComponentValue( (BaseResource) targetPage );
                    targetPage.getValues().add( cv );
                }
                if( cv == null ) {
                    log.debug( "Didnt find: " + paramName );
                    return "";
                } else {
//                    log.debug("rendering cv:" + getEditMode() + " - " + editable);
                    if( editable ) {
                        s = cv.renderEdit( childRc );
                    } else {
                        s = cv.render( childRc );
                    }
                    if( s == null ) {
                        s = "";
                    }
                    //log.debug( "!editmod " + !getEditMode() + " markers:" + markers);
                    if( !getEditMode() && markers ) {
                        return wrapWithIdentifier( s, def.getName() );
                    } else {
                        return s;
                    }
                }
            } else {
//                log.debug("not a componentdef: " + c.getClass());
                if( editable ) {
                    s = c.renderEdit( childRc );
                } else {
                    s = c.render( childRc );
                }
            }
            if( s == null ) s = "";
            return s;
        } catch( Exception e ) {
            log.error( "exception invoking: " + paramName, e );
            return "ERR: " + paramName + " : " + e.getMessage();
        }
    }

    public String invoke( Templatable page, String paramName ) {
        RenderContext rc = new RenderContext( page.getTemplate(), page, null, false );
        return rc.invoke( paramName );
    }

    public String invoke( String paramName ) {
        return invoke( paramName, ( child != null && child.editMode ), true );
    }

    public String invokeForEdit( String paramName ) {
        if( child != null && child.editMode ) {
            return invoke( paramName, true );
        } else {
            return "";
        }
    }

    public String invoke( Component c, boolean editable ) {
        String s = c.renderEdit( child );
        if( s == null ) {
            s = "";
        }
        return s;

    }

    public String invoke( Component c ) {
        return c.render( child );
    }

    public ComponentValue getComponentValue( String name, Templatable page ) {
        if( page == null ) {
            return null;
        }
        ComponentValue cv = page.getValues().get( name );
        if( cv != null ) {
            if( cv.getContainer() == null ) {
                cv.init( page );
            }
            return cv;
        }
        return getComponentValue( name, page.getTemplate() );
    }

    public String invokeEdit( String paramName ) {
        Component c = this.getTargetPage().getComponent( paramName, false );
        if( c != null ) {
            return c.renderEdit( child );
        }
        ComponentValue cv;
        if( child != null && child.getMe() != null ) {
            cv = child.getMe().getValues().get( paramName );
        } else {
            cv = new ComponentValue( paramName, null );
        }
        if( cv == null ) {
            log.error( "no parameter " + paramName + " in param values from " + child.getMe().getName() );
            return null;
        }
        return cv.renderEdit( child );
    }

    /** Return html for the child's body
     */
    public String getToolBar() {
        StringBuffer sb = new StringBuffer();
        Templatable targetPage = getTargetPage();
        Collection<Component> list = CommonTemplated.allComponents( targetPage );
        for( Component c : list ) {
            if( c instanceof Command ) {
                Command cmd = (Command) c;
                sb.append( cmd.render( child ) );
            }
        }
        return sb.toString();
    }

    public boolean isEmpty( Object o ) {
        if( o == null ) {
            return true;
        } else if( o instanceof String ) {
            String s = (String) o;
            return s.trim().length() == 0;
        } else if( o instanceof Collection ) {
            Collection col = (Collection) o;
            return col.size() == 0;
        } else if( o instanceof Map ) {
            Map m = (Map) o;
            return m.size() == 0;
        } else {
            return true;
        }
    }

    public Date getNow() {
        return new Date();
    }

    public Templatable find( String path ) {
        return RenderContext.find( page, Path.path( path ) );
    }

    public Component findComponent( Path path ) {
        return findComponent( path, page );
    }

    public static Component findComponent( Path path, Templatable page ) {
//        log.debug("findComponent: " + path + " from: " + page.getName());

        Component c = null;
        if( !path.isRelative() ) {
            c = findComponentWithRelativePath( path, page.getWeb() );
        } else {
            c = findComponentWithRelativePath( path, page );
        }
        return c;

    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey( Object key ) {
        return false;
    }

    @Override
    public boolean containsValue( Object value ) {
        return false;
    }

    @Override
    public Component get( Object key ) {
        String sKey = key.toString();
        Component c = page.getComponent( sKey, false );
        if( c != null ) {
            return c;
        }
        ComponentValue cv = page.getValues().get( sKey );
        if( cv != null ) {
            return cv;
        }
        return null;
    }

    @Override
    public Component put( String key, Component value ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Component remove( Object key ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public void putAll( Map<? extends String, ? extends Component> m ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public void clear() {

        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Collection<Component> values() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Set<Entry<String, Component>> entrySet() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public static Component findComponentWithRelativePath( Path path, Templatable startFrom ) {
        return findComponentWithRelativePath( startFrom, path.getParts(), 0 );
    }

    private static Component findComponentWithRelativePath( Object parent, String[] arr, int i ) {
        if( arr.length == 0 ) {
            return null;
        }
        //log.debug( "findComponentWithRelativePath: " + arr.length + " " + i);
        String childName = arr[i];
        Object child = null;
        if( parent instanceof Resource ) {
            Resource r = (Resource) parent;
            //log.debug( "find child: " + childName);
            child = ExistingResourceFactory.findChild( r, childName );
        }
        if( child == null ) {
            if( parent instanceof ComponentContainer ) {
                ComponentContainer cc = (ComponentContainer) parent;
                //log.debug( "find any component: " + childName);
                child = cc.getAnyComponent( childName );
            } else {
                //log.debug( "not a ComponentContainer: " + parent.getClass());
            }
        }
        if( child == null ) {
            //log.debug( "no child");
            return null;
        } else {
            if( i < arr.length - 1 ) {
                return findComponentWithRelativePath( child, arr, i + 1 );
            } else {
                if( child instanceof Component ) {
                    return (Component) child;
                } else {
                    log.warn( "Found something, not a component thoug: " + child );
                    return null;
                }
            }
        }
    }

    public static Templatable findPageWithRelativePath( Path path, Templatable page ) {
        if( path == null ) {
            return page;
        }
        Resource r = ExistingResourceFactory.findChild( page, path );
        if( r instanceof Templatable ) {
            return (Templatable) r;
        }
        return null;
    }

    String wrapWithIdentifier( String s, String name ) {
        // oxygen xml doesnt send auth data for GETs of publi
//        if( RequestParams.current().getAuth() == null ) {
//            log.debug( "no logged in user, so not wrapping with markers");
//            return s;
//        }
        Templatable t = getTargetPage();
        String ct = t.getContentType( null );
        if( ct == null || ct.trim().length() == 0 || ct.equals( "text/html" ) ) { // ct==null means prolly template
            // interfere's with xml
            log.debug( "ct: " + ct );
            return PARSER.addMarkers( s, name );
        } else {
            log.debug( "not ct: " + ct );
            return s;
        }
    }

    public BaseResource getPhysicalPage() {
        Templatable ct = getTargetPage();
        if( ct instanceof BaseResource ) {
            return (BaseResource) ct;
        } else {
            return ct.getParentFolder();
        }
    }
//    public String include(String path) {
//        log.debug( "include: " + path);
//        Path p = Path.path( path );
//        Component c = findComponent( p, page );
//        ComponentDef cdef;
//        if( c == null ) {
//            log.warn( "component not found: " + path + " from: " + page.getHref());
//            return "";
//        } else if( c instanceof ComponentValue) {
//            ComponentValue cv = (ComponentValue) c;
//            CommonTemplated ct = (CommonTemplated) cv.getContainer();
//            Template t = ct.getTemplate();
//            if( t != null ) {
//                cdef = ct.getTemplate().getComponentDef( cv.getName());
//                if( cdef != null ) {
//                    log.debug( "rendering from def");
//                    log.debug( "cv:  " + cv.getValue());
//                    log.debug( "cv parent: " + ct.getHref());
//                    RenderContext rcInclude = new RenderContext( t, ct, null, false);
//                    String s = cdef.render( cv, rcInclude );
//                    return s;
//                } else {
//                    return  "component def not found. name: " + cv.getName() + " in template: "  +t.getName();
//                }
//            } else {
//                return  "template not found: " + ct.getTemplateName();
//            }
//        } else {
//            return "path did not return a componentvalue. returned a: " + c.getClass();
//        }
//    }
}

