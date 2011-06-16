package com.bradmcevoy.web.children;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.SubPage;
import com.bradmcevoy.web.Templatable;
import java.util.ArrayList;
import java.util.List;

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

    public List<Templatable> getSubPages(Folder folder) {
        List<Templatable> list = new ArrayList<Templatable>();
        addSubPages(list, folder);
        return list;
    }

    private void addSubPages(List<Templatable> list, Templatable t) {
        for( Component c : t.getComponents().values() ) {
            if( c instanceof SubPage ) {
                SubPage sp = (SubPage) c;
                if( sp.isBrowsable()){
                    list.add(sp);
                }
            }
        }
        ITemplate next = t.getTemplate();
        if( next != null ) {
            addSubPages(list, next);
        }
    }

}
