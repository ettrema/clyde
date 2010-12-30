package com.bradmcevoy.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.ComponentContainer;
import com.bradmcevoy.web.ExistingResourceFactory;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.Templatable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ComponentUtils {

    public static boolean validateComponents( Object target, RenderContext rc ) {
        Map<String, Field> fields = InitUtils.componentFields( target );
        boolean b = true;
        for( Field f : fields.values() ) {
            Component c = InitUtils.getComponent( f, target );
            b = b && c.validate( rc );
        }
        return b;
    }

    public static boolean validatePage( CommonTemplated page, RenderContext rc ) {
        boolean ok = true;
        for( Component v : page.allComponents() ) {
            if( v instanceof Command ) {
                // ignore
            } else {
                ok = ok && v.validate( rc );
            }
        }
        return ok;
    }

    /**
     * Eg find a component called
     *
     * @param path
     * @param startFrom
     * @return
     */
    public static Component findComponent( Path path, Templatable startFrom ) {
        if( !path.isRelative() ) {
            startFrom = startFrom.getWeb();
        }
        ComponentContainer parent = findContainer( path.getParent(), startFrom );
        if( parent == null ) {
            return null;
        } else {
            Component c = parent.getAnyComponent( path.getName() );
            return c;
        }
    }

    public static ComponentContainer findContainer( Path path, Templatable from ) {
        if( path == null || path.isRoot() ) {
            return from;
        } else {
            ComponentContainer parent = findContainer( path.getParent(), from );
            if( parent == null ) {
                return null;
            } else {
                if( parent instanceof Resource ) {
                    Resource rParent = (Resource) parent;
                    Resource child = ExistingResourceFactory.findChild( rParent, path.getName() );
                    if( child != null ) {
                        if( child instanceof ComponentContainer ) {
                            return (ComponentContainer) child;
                        } else {
                            return null;
                        }
                    }
                }
                Component c = parent.getAnyComponent( path.getName() );
                if( c instanceof ComponentContainer ) {
                    return (ComponentContainer) c;
                } else {
                    return null;
                }

            }

        }
    }

    public static List<Component> getAllComponents(Templatable page) {
        List<Component> list = new ArrayList<Component>();
        addComponents(page, list);
        return list;
    }

    private static void addComponents( Templatable page, List<Component> list ) {
        if( page == null ) {
            return ;
        }
        for( Component c: page.getValues().values()) {
            list.add( c );
        }
        for( Component c: page.getComponents().values() ) {
            list.add( c );
        }
        addComponents( page.getTemplate(), list);
    }


    public static Collection<Component> allComponents( Templatable res ) {
        Map<String, Component> map = new HashMap<String, Component>();
        ITemplate parentTemplate = res.getTemplate();
        if( parentTemplate != null ) {
            addInheritedComponents( parentTemplate, map );
        }
        for( ComponentValue cv : res.getValues().values() ) {
            map.put( cv.getName(), cv );
        }
        for( Component c : res.getComponents().values() ) {
            map.put( c.getName(), c );
        }
        Set set = new HashSet( map.values() );
        List<Component> list = new ArrayList<Component>();
        list.addAll( set );
        Collections.sort( list, new com.bradmcevoy.web.ComponentComparator() );
        return list;
    }

    private static void addInheritedComponents( Templatable res, Map<String, Component> map ) {
        if( res == null ) {
            return;
        }
        ITemplate p = res.getTemplate();
        addInheritedComponents( p, map );
        for( Component c : res.getComponents().values() ) {
            map.put( c.getName(), c );
        }
    }

    /**
     * Recursively looks for a component (or ComponentValue if includeValues is true)
     * from the given resource and templates
     *
     * @param res
     * @param paramName
     * @param includeValues
     * @return
     */
    public static Component getComponent( Templatable res,String paramName, boolean includeValues ) {
        Component c;
        if( includeValues ) {
            c = res.getValues().get( paramName );
            if( c != null ) {
                return c;
            }
        }
        c = res.getComponents().get( paramName );
        if( c != null ) {
            return c;
        }

        ITemplate t = res.getTemplate();
        if( t == null ) {
            return null;
        }

        c = getComponent( t, paramName, includeValues );
        if( c != null ) {
            if( c instanceof WrappableComponent ) {
                return new WrappedComponent( res, (WrappableComponent) c );
            } else {
                return c;
            }
        } else {
            return null;
        }
    }

}
