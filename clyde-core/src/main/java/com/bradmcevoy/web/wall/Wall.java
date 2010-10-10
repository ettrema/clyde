package com.bradmcevoy.web.wall;

import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class Wall implements DataNode, Serializable, Iterable<WallItem> {

    private static final long serialVersionUID = 1L;
    private UUID id;
    private transient NameNode nameNode;
    private List<WallItem> wallItems;

    public void setId( UUID id ) {
        System.out.println( "------- setId: " + id );
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void init( NameNode nameNode ) {
        this.nameNode = nameNode;
    }

    public void onDeleted( NameNode nameNode ) {

    }

    public void save() {
        nameNode.save();
    }

    public void addItem( FolderUpdateWallItem fu ) {
        if( wallItems == null ) {
            wallItems = new ArrayList<WallItem>();
        }
        wallItems.add( fu );
    }

    public Iterator<WallItem> iterator() {
        if( wallItems == null ) {
            wallItems = new ArrayList<WallItem>();
        }
        return wallItems.iterator();
    }

    public int size() {
        if( wallItems == null ) {
            return 0;
        }
        return wallItems.size();
    }

    public void remove( WallItem oldest ) {
        wallItems.remove( oldest );
    }

    NameNode getNameNode() {
        return nameNode;
    }


}
