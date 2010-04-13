package com.bradmcevoy.web;

import com.bradmcevoy.utils.ReflectionUtils;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.Text;
import java.io.InputStream;
import java.util.Collection;
import org.jdom.Element;

public class Template extends Page implements ITemplate {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Template.class );
    private static final long serialVersionUID = 1L;

    static ITemplate lookup( String templateName, Web web ) {
//        log.debug( "lookup: " + templateName + " in " + web.getPath());
        if( templateName == null ) return null;
        Folder templates = web.getTemplates();
//        log.debug( "templates: " + templates.getPath());
        if( templates == null ) {
            throw new NullPointerException( "No templates folder for web: " + web.getName() );
        }
        if( templateName.equals( "root" ) ) {
            return Root.getInstance( templates );
        }
        if( templateName.equals( "rootTemplate.html" ) ) {
            return new RootTemplate( templates );
        } else {
            Template template = null;
            BaseResource res = templates.childRes( templateName );  // note: do not call .child(..) here since that will check for components, and possibly result in infinite loop
            if( res != null && !( res instanceof Template ) ) {
                throw new RuntimeException( "not a Template: " + res.getPath() + " is a: " + res.getClass() );
            }
            template = (Template) res;
            if( template == null ) {
                Web parentWeb = web.getParentWeb();
                if( parentWeb != null && parentWeb != web ) {
                    ITemplate t = lookup( templateName, parentWeb );
                    if( t != null ) {
                        return new WrappedTemplate( t, web );
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                if( template.getWeb() != web ) {
                    return new WrappedTemplate( template, web );
                } else {
                    return template;
                }
            }
        }
    }
    private final ComponentDefMap componentDefs = new ComponentDefMap();
//    private transient Component addParam;

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
    public void populateXml( Element e2 ) {
        super.populateXml( e2 );
        componentDefs.toXml( this, e2 );
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
                log.debug("  creating a page because nothing else specified");
                newRes = new Page( location, name );
            } else {
                log.debug("  created a: " + newRes.getClass());
            }
        }
        newRes.setTemplate( this );
        for( ComponentDef def : componentDefs.values() ) {
            ComponentValue cv = def.createComponentValue( newRes );
            newRes.getValues().add( cv );
        }
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
            log.debug( "createFolderFromTemplate: created a: " + cv.getClass() + " def:" + def.getName());
            newRes.getValues().add( cv );
        }
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
        Component c = this.getComponent( "class" );
        String sClass = null;
        if( c != null ) {
            Text t = (Text) c;
            sClass = t.getValue();
            log.debug( "component source: " + t.getPath() );
        }
        if( sClass == null ) {
            return null;
        } else {
            sClass = sClass.trim();
            log.debug( "creating a '" + sClass + "'" );
            Class clazz = ReflectionUtils.findClass( sClass );
            return (BaseResource) ReflectionUtils.create( clazz, location, name );
        }
    }

    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new Template( parent, newName );
    }

    @Override
    public ComponentDefMap getComponentDefs() {
        return componentDefs;
    }
}
