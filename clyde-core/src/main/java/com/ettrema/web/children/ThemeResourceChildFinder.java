package com.ettrema.web.children;

import com.bradmcevoy.http.Resource;
import com.ettrema.web.Folder;
import com.ettrema.web.Templatable;
import com.ettrema.web.Web;
import java.util.List;

/**
 * Implement locating themed resources from under the templates folder
 * in a web which has a theme
 *
 * @author brad
 */
public class ThemeResourceChildFinder implements ChildFinder {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ThemeResourceChildFinder.class );
    private final ChildFinder wrapped;
    private final ThemeFinder themeFinder;

    public ThemeResourceChildFinder( ChildFinder wrapped, ThemeFinder themeFinder ) {
        this.wrapped = wrapped;
        this.themeFinder = themeFinder;
    }

	@Override
    public Resource find( String name, Folder folder ) {
        log.trace( "find" );
        Resource r = wrapped.find( name, folder );
        if( r != null ) {
            return r;
        }

        if( folder instanceof Web && "templates".equals( name ) ) {
            log.trace( "isWeb" );
            Folder themeFolder = themeFinder.getThemeFolder( (Web)folder );
            if( themeFolder == null ) {
                log.trace( "no theme folder" );
                return null;
            } else {
                return themeFolder;
            }
        } else if( isTemplateFolder( folder ) ) {
            log.trace( "isTemplateFolder" );
            Folder themeFolder = themeFinder.getThemeFolder( folder.getWeb() );
            if( themeFolder == null ) {
                log.trace( "no theme folder" );
                return null;
            } else {
                return themeFolder.child( name );
            }
        } else {
            log.trace( "not a theme folder" );
            return null;
        }

    }

    private boolean isTemplateFolder( Folder folder ) {
        if( !folder.getName().equals( "templates" ) ) {
            return false;
        }
        return ( folder.getParent() instanceof Web );
    }

    public List<Templatable> getSubPages(Folder folder) {
        return wrapped.getSubPages(folder);
    }
}
