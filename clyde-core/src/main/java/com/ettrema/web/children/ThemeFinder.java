package com.ettrema.web.children;

import com.bradmcevoy.http.Resource;
import com.ettrema.web.Folder;
import com.ettrema.web.Web;

/**
 *
 * @author brad
 */
public class ThemeFinder {
    public Folder getThemeFolder(Web web) {
        String themeName = web.selectedThemeName();
        Folder themes = null;
        if( themeName != null && themeName.length() > 0 ) {
//            log.debug( "looking for themes: " + themeName);
            themes = web.getThemes();
        }
        if( themes != null ) {
            Resource res = themes.child( themeName );
            if( res instanceof Folder ) {
//                log.debug( "using theme folder for templates: " + res.getName());
                return (Folder) res;
            }
        }
        return null;
    }
}
