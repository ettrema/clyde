package com.bradmcevoy.web.calendar;

import com.bradmcevoy.http.ReportableResource;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.Relationship;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.IUser;
import com.ettrema.http.AccessControlledResource;
import com.ettrema.http.CalendarResource;
import java.util.List;

/**
 *
 */
public class Calendar extends Folder implements CalendarResource, AccessControlledResource, ReportableResource {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Calendar.class );
    private static final long serialVersionUID = 1L;

    public Calendar( Folder parentFolder, String name ) {
        super( parentFolder, name );
    }

    @Override
    protected BaseResource copyInstance( Folder parent, String newName ) {
        Calendar uNew = (Calendar) super.copyInstance( parent, newName );
        return uNew;
    }

    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new Calendar( parent, newName );
    }

    public String getPrincipalURL() {
        IUser owner = getOwner();
        if( owner == null ) {
            return null;
        } else {
            return owner.getHref();
        }
    }

    public IUser getOwner() {
        List<Relationship> list = this.getNameNode().findFromRelations("principalOwner");
        if( list == null || list.size() == 0 ) {
            return null;
        } else {
            Relationship rel = list.get(0);
            NameNode node = rel.to();
            if( node == null ) {
                log.warn("Got null to node for owner");
                return null;
            } else {
                return (IUser) node.getData();
            }
        }
    }

    public void setOwner(IUser owner) {
        this.getNameNode().makeRelation(owner.getNameNode(), "principalOwner");
    }
}
