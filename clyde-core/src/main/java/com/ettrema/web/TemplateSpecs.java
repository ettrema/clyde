package com.ettrema.web;

import com.bradmcevoy.http.Resource;
import com.ettrema.web.TemplateSpecs.TemplateSpec;
import com.ettrema.web.security.PermissionRecipient.Role;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TemplateSpecs extends ArrayList<TemplateSpec> implements Serializable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( TemplateSpecs.class );
    private static final long serialVersionUID = 1L;

    /**
     * Find the TemplateSpecs which should be used for the given folder. The
     * specs defined diretly on a folder may be null, in which case
     * you should delegate to the parent, and so on.
     * 
     * This method performs the recursive search up to the Host
     * 
     * @param thisFolder
     * @return
     */
    public static TemplateSpecs getSpecsToUse( Folder thisFolder ) {
        log.trace( "getSpecsToUse" );
        TemplateSpecs specs = getFromResourceOrTemplate( thisFolder );
        Folder parent;
        if( thisFolder instanceof Host ) {
            if( log.isTraceEnabled() ) {
                log.trace( "return template specs from: " + thisFolder.getName() );
            }
            return specs;
        } else {
            if( specs == null || specs.isEmpty() ) {
                parent = thisFolder.getParent();
                if(  parent == null ) {
                    return null;
                } else {
                    return getSpecsToUse( thisFolder.getParent() );
                }
            } else {
                if( log.isTraceEnabled() ) {
                    log.trace( "return template specs from: " + thisFolder.getName() );
                }
                return specs;
            }
        }
    }

    /**
     * Locate template specs defined directly on the resource or on a component from its parent
     *
     * @param folder
     * @return
     */
    private static TemplateSpecs getFromResourceOrTemplate( Folder folder ) {
        if( folder.templateSpecs == null || folder.templateSpecs.isEmpty() ) {
            log.trace( "getFromResourceOrTemplate: no templateSpecs direct on folder" );
            Component c = folder.getComponent( "allowedTemplates", false );
            if( c != null ) {
                log.trace( " - got component" );
                String val = c.toString();
                return TemplateSpecs.parse( val );
            } else {
                return null;
            }
        } else {
            log.trace( "found templateSpecs direct on resource" );
            return folder.templateSpecs;
        }
    }

    /**
     * 
     * @param thisFolder
     * @return - all templates from the folder's web
     */
    public static List<Template> findApplicable( Folder thisFolder ) {
        Web web = thisFolder.getWeb();
        if( web == null ) {
            log.warn( "no web for: " + thisFolder.getPath() );
            return null;
        }
        Folder templatesFolder = web.getTemplates();
        if( templatesFolder == null ) {
            log.warn( "no templates for web: " + web.getPath() );
            return null;
        }
        List<? extends Resource> list = thisFolder.getWeb().getTemplates().getChildren();
        List<Template> list2 = new ArrayList<Template>();
        for( Resource r : list ) {
            if( r instanceof Template ) {
                list2.add( (Template) r );
            }
        }
        return list2;
    }

    public static TemplateSpecs parse( String s ) {
        return new TemplateSpecs( s );
    }

    public static TemplateSpec parseSpec( String sSpec ) {
        if( sSpec == null || sSpec.length() == 0 ) return null;
        sSpec = sSpec.trim();
        Role createRole = null;
        Role editRole = null;
        if( sSpec.contains( "(" ) ) {
            if( sSpec.endsWith( ")" ) ) {
                String roles = sSpec.substring( sSpec.indexOf( "(" ) + 1, sSpec.indexOf( ")" ) );
                sSpec = sSpec.substring( 0, sSpec.indexOf( "(" ) );
                String[] arrRoles = roles.split( "," );
                if( arrRoles.length > 2 ) {
                    throw new RuntimeException( "Invalid template spec. Too many roles. Expected 2 or less, was: " + arrRoles.length );
                }
                if( arrRoles.length > 0 ) {
                    createRole = parseRole( arrRoles[0] );
                }
                if( arrRoles.length > 1 ) {
                    editRole = parseRole( arrRoles[1] );
                }
            } else {
                throw new RuntimeException( "Invalid template spec. Did not find closing bracket at end" );
            }
        }
        if( sSpec.startsWith( "-" ) ) {
            sSpec = sSpec.substring( 1 );
            return new DisallowTemplateSpec( sSpec );
        } else if( sSpec.startsWith( "+" ) ) {
            sSpec = sSpec.substring( 1 );
            return new AllowTemplateSpec( sSpec, createRole, editRole );
        } else {
            return new AllowTemplateSpec( sSpec, createRole, editRole );
        }
    }

    private static Role parseRole( String s ) {
        try {
            return Role.valueOf( s );
        } catch( Throwable e ) {
            throw new RuntimeException( "Couldnt parse role name: " + s );
        }
    }

    public TemplateSpecs() {
    }

    public TemplateSpecs( String specs ) {
        if( specs == null ) return;
        String[] arr = specs.split( " " );
        for( String s : arr ) {
            TemplateSpec spec = parseSpec( s );
            if( spec != null ) this.add( spec );
        }
    }

    /**
     * Find the allowed spec for the given template name
     * 
     * @param folder - the folder in which the template might apply (if allowed)
     * @param template - the template for which we want to find the specs
     * @return - null, if not allowed or there is no particlar spec. Otherwise the spec which allows the template
     */
    public AllowTemplateSpec findAllowedSpec( Folder thisFolder, ITemplate template ) {
        for( TemplateSpec spec : this ) {
            Boolean allow = spec.allow( template );
            if( allow != null ) {
                if( allow ) {
                    return (AllowTemplateSpec) spec;
                } else {
                    return null;
                }
            }
        }
        return null;

//        TemplateSpecs specsToUse = getSpecsToUse( thisFolder );
//        if( specsToUse == null || specsToUse.isEmpty() ) {
//            return null;
//        } else {
//            for( TemplateSpec spec : specsToUse ) {
//                Boolean allow = spec.allow( template );
//                if( allow != null ) {
//                    if( allow ) {
//                        return (AllowTemplateSpec) spec;
//                    } else {
//                        return null;
//                    }
//                }
//            }
//            return null;
//        }

    }

    public TemplateSpec add( String s ) {
        TemplateSpec spec = parseSpec( s );
        this.add( spec );
        return spec;
    }

    public List<Template> findAllowed( Folder thisFolder ) {
        List<Template> list = findApplicable( thisFolder );
        if( list == null ) return null;

        TemplateSpecs specsToUse = getSpecsToUse( thisFolder );
        if( specsToUse == null || specsToUse.isEmpty() ) {
            return list;
        } else {
            return specsToUse.findAllowed( list );
        }
    }

    public List<Template> findAllowedDirect( Folder thisFolder ) {
        List<Template> list = findApplicable( thisFolder );
        if( list == null ) return null;
        return findAllowed( list );
    }

    public List<Template> findAllowed( List<Template> all ) {
        List<Template> allowed = new ArrayList<Template>( all );
        Iterator<Template> it = allowed.iterator();
        while( it.hasNext() ) {
            if( !isAllowed( it.next() ) ) {
                it.remove();
            }
        }
        return allowed;
    }

    public boolean isAllowed( Template t ) {
        for( TemplateSpec spec : this ) {
            Boolean b = spec.allow( t );
            if( b != null ) return b;
        }
        return true;
    }

    public String format() {
        StringBuffer sb = new StringBuffer();
        for( TemplateSpec spec : this ) {
            if( spec != null ) {
                spec.append( sb );
                sb.append( " " );
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return format();
    }

    public static abstract class TemplateSpec implements Serializable {

        private static final long serialVersionUID = 1L;
        final String pattern;

        /**
         * Determine whether this rule applies, and if so what the answer is
         * 
         * @param t
         * @return - True, False, or null means don't care
         */
        abstract Boolean allow( ITemplate t );

        abstract void append( StringBuffer sb );

        public TemplateSpec( String pattern ) {
            this.pattern = pattern;
        }
    }

    public static class AllowTemplateSpec extends TemplateSpec implements Serializable {

        private static final long serialVersionUID = 1L;
        Role createNewRole;
        Role editRole;

        public AllowTemplateSpec( String pattern, Role createNewRole, Role editRole ) {
            super( pattern );
            this.createNewRole = createNewRole;
            this.editRole = editRole;
        }

        @Override
        public Boolean allow( ITemplate t ) {
            if( t == null ) {
                return Boolean.TRUE;
            } else if( pattern == null || pattern.equals( "*" ) ) {
                return Boolean.TRUE;
            } else {
                if( pattern.equals( t.getName() ) ) {
                    return Boolean.TRUE;
                } else {
                    return null;
                }
            }
        }

        @Override
        void append( StringBuffer sb ) {
            sb.append( "+" ).append( pattern );
            if( createNewRole != null || editRole != null ) {
                sb.append( "(" );
                if( createNewRole != null ) {
                    sb.append( createNewRole.name() );
                }
                sb.append( "," );
                if( editRole != null ) {
                    sb.append( editRole.name() );
                }
                sb.append( ")" );
            }
        }

        public String getPattern() {
            return pattern;
        }

        public Role getCreateNewRole() {
            return createNewRole;
        }

        public Role getEditRole() {
            return editRole;
        }
    }

    public static class DisallowTemplateSpec extends TemplateSpec implements Serializable {

        private static final long serialVersionUID = 1L;

        public DisallowTemplateSpec( String pattern ) {
            super( pattern );
        }

        @Override
        public Boolean allow( ITemplate t ) {
            if( pattern.equals( "*" ) ) {
                return Boolean.FALSE;
            } else {
                if( pattern.equals( t.getName() ) ) {
                    return Boolean.FALSE;
                } else {
                    return null;
                }
            }
        }

        @Override
        void append( StringBuffer sb ) {
            sb.append( "-" ).append( pattern );
        }
    }
}
