package com.bradmcevoy.web;

import com.bradmcevoy.utils.ReflectionUtils;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.EmailDef;
import com.bradmcevoy.web.component.HtmlDef;
import com.bradmcevoy.web.component.NumberDef;
import com.bradmcevoy.web.component.Text;
import com.bradmcevoy.web.component.TextDef;
import com.bradmcevoy.web.security.CurrentUserService;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;


import static com.ettrema.context.RequestContext._;

public class Template extends Page implements ITemplate {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Template.class );
    private static final long serialVersionUID = 1L;
    private final ComponentDefMap componentDefs = new ComponentDefMap();
//    private transient Component addParam;
    private String afterCreateScript;

    public Template( Folder parent, String name ) {
        super( parent, name );
    }

    @Override
    protected BaseResource copyInstance( Folder parent, String newName ) {
        Template newRes = (Template) super.copyInstance( parent, newName );
        newRes.componentDefs.addAll( this.componentDefs );
        return newRes;
    }

    @Override
    protected void initComponents() {
        super.initComponents();
        componentDefs.init( this );
    }

    @Override
    public void loadFromXml( Element el ) {
        super.loadFromXml( el );
        componentDefs.fromXml( this, el );
        Element elScript = el.getChild( "afterCreateScript" );
        if( elScript != null ) {
            afterCreateScript = elScript.getText();
        }
    }

    @Override
    public void populateXml( Element e2 ) {
        super.populateXml( e2 );
        componentDefs.toXml( this, e2 );
        if( afterCreateScript != null ) {
            Element elScript = new Element( "afterCreateScript" );
            elScript.setText( afterCreateScript );
            e2.addContent( elScript );
        }
    }

    @Override
    public Element toXml( Addressable container, Element el ) {
        return super.toXml( container, el );
    }

    @Override
    public boolean is( String type ) {
        if( type.equalsIgnoreCase( "template" ) ) return true;
        return super.is( type );
    }

    @Override
    public boolean represents( String type ) {
        String tname = getName();
        boolean b = type.equals( tname ) || ( type + ".html" ).equals( tname );
        if( b ) {
            return true;
        }
        ITemplate parent = getTemplate();
        if( parent != null ) {
            return parent.represents( type );
        } else {
            return false;
        }

    }

    @Override
    public Component getAnyComponent( String childName ) {
        // not, for rendercontext.invoke to work properly, this must return cdef's in preference to cvalues
        Component c = getComponentDef( childName );
        if( c != null ) return c;

        return super.getAnyComponent( childName );
    }

    @Override
    public ComponentDef getComponentDef( String name ) {
        ComponentDef def = getComponentDefs().get( name );
        if( def != null ) {
            return def;
        }
        ITemplate t = this.getTemplate();
        if( t == null ) {
            return null;
        }
        return t.getComponentDef( name );
    }

    @Override
    public BaseResource createPageFromTemplate( Folder location, String name, InputStream in, Long length ) {
        BaseResource res = createPageFromTemplate( location, name );
        res.save();
        res.setContent( in );
        return res;
    }

    /**
     * 
     * @param location
     * @param name
     * @return - a newly created, but not saved, baseresource
     */
    @Override
    public BaseResource createPageFromTemplate( Folder location, String name ) {
        log.debug( "createPageFromTemplate" );
        BaseResource newRes;
        if( location.getName().equals( this.getParent().getName() ) ) {
//            log.debug("  creating a template, because in templates folder"); // hack alert
            newRes = new Template( location, name );
        } else {
            newRes = newInstanceFromTemplate( location, name );
            if( newRes == null ) {
                log.debug( "  creating a page because nothing else specified" );
                newRes = new Page( location, name );
            } else {
                log.debug( "  created a: " + newRes.getClass() );
            }
        }
        IUser creator = _( CurrentUserService.class ).getOnBehalfOf();
        if( creator instanceof User ) {
            newRes.setCreator( (User) creator );
        }

        newRes.setTemplate( this );
        for( ComponentDef def : componentDefs.values() ) {
            ComponentValue cv = def.createComponentValue( newRes );
            newRes.getValues().add( cv );
        }
        execAfterScript( newRes );
        return newRes;
    }

    @Override
    public Folder createFolderFromTemplate( Folder location, String name ) {
        log.debug( "createFolderFromTemplate" );
        Folder newRes;
        newRes = (Folder) newInstanceFromTemplate( location, name );
        if( newRes == null ) {
            newRes = new Folder( location, name );
        }

        newRes.setTemplate( this );

        for( ComponentDef def : componentDefs.values() ) {
            ComponentValue cv = def.createComponentValue( newRes );
            log.debug( "createFolderFromTemplate: created a: " + cv.getClass() + " def:" + def.getName() );
            newRes.getValues().add( cv );
        }
        execAfterScript( newRes );
        return newRes;
    }

    @Override
    public Collection<Component> allComponents() {
        Collection<Component> set = super.allComponents();
        set.addAll( componentDefs.values() );
//        set.add( getAddParameterComponent() );
        return set;
    }

    private BaseResource newInstanceFromTemplate( Folder location, String name ) {
        if( location == null )
            throw new IllegalArgumentException( "location is null" );
        if( name == null )
            throw new IllegalArgumentException( "name cannot be null" );
        String sClass = getClassToCreate();
        if( sClass == null ) {
            return null;
        } else {
            log.debug( "creating a '" + sClass + "' called: " + name );
            Class clazz = ReflectionUtils.findClass( sClass );
            return (BaseResource) ReflectionUtils.create( clazz, location, name );
        }
    }

    public boolean canCreateFolder() {
        String s = getClassToCreate();
        if( StringUtils.isEmpty( s ) ) {
            return false;
        } else {
            return Folder.class.getCanonicalName().equals( s );
        }
    }

    public String getClassToCreate() {
        Component c = this.getComponent( "class" );
        String sClass = null;
        if( c != null ) {
            Text t = (Text) c;
            sClass = t.getValue();
        }
        if( sClass == null ) {
            return null;
        } else {
            return sClass.trim();
        }
    }

    public void setClassToCreate( String s ) {
        Text c = (Text) this.getComponent( "class" );
        if( c == null ) {
            c = new Text( this, "class" );
            this.getComponents().add( c );
        }
        c.setValue( s );
    }

    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new Template( parent, newName );
    }

    @Override
    public ComponentDefMap getComponentDefs() {
        return componentDefs;
    }

    private void execAfterScript( BaseResource newlyCreated ) {
        if( afterCreateScript == null ) return;
        log.debug( "execAfterScript" );
        Map map = new HashMap();
        map.put( "created", newlyCreated );
        map.put( "command", this );
        Templatable ct = (Templatable) this.getContainer();
        exec( ct, map, afterCreateScript );
        log.debug( "done execAfterScript" );
    }

    private void exec( Templatable ct, Map map, String expr ) {
        try {
            BaseResource targetContainer = CommonTemplated.getTargetContainer();
            map.put( "targetPage", targetContainer );
            map.put( "formatter", Formatter.getInstance() );
            org.mvel.MVEL.eval( expr, ct, map );
        } catch( Exception e ) {
            throw new RuntimeException( "Exception evaluating expression: " + expr + " in page: " + ct.getName(), e );
        }
    }

    public TextDef addTextDef( String name ) {
        TextDef d = new TextDef( this, name );
        this.componentDefs.add( d );
        return d;
    }

    public NumberDef addNumberDef( String name ) {
        NumberDef d = new NumberDef( this, name );
        this.componentDefs.add( d );
        return d;
    }

    public HtmlDef addHtmlDef( String name ) {
        HtmlDef d = new HtmlDef( this, name );
        this.componentDefs.add( d );
        return d;
    }

    public EmailDef addEmailDef( String name ) {
        EmailDef d = new EmailDef( this, name );
        this.componentDefs.add( d );
        return d;
    }

    public String getAfterCreateScript() {
        return afterCreateScript;
    }
}
