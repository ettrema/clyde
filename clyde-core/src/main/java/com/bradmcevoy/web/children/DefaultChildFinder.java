package com.bradmcevoy.web.children;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.Folder;

/**
 *
 * @author brad
 */
public class DefaultChildFinder implements ChildFinder{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( DefaultChildFinder.class );

    public Resource find( String name, Folder folder ) {
        Resource res = folder.childRes( name );
        if( res != null ) {
            return res;
        }

        Component c = folder.getComponent( name );
        if( c instanceof Resource ) {
            if( folder instanceof BaseResource ) {
//                log.debug( "setting target container: " + this.getHref() );
                CommonTemplated.tlTargetContainer.set( folder ); // arghhh
            } else {
                //log.debug( "not setting: " + this.getClass() );
            }

            return (Resource) c;
        }
        return null;
    }

}
