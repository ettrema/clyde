package com.ettrema.web.wall;

import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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

	@Override
    public void setId( UUID id ) {
        this.id = id;
    }

	@Override
    public UUID getId() {
        return id;
    }

	@Override
    public void init( NameNode nameNode ) {
        this.nameNode = nameNode;
    }

	@Override
    public void onDeleted( NameNode nameNode ) {

    }

    public void delete() {
        nameNode.delete();
    }

    public void save() {
        nameNode.save();
    }

    public void addItem( WallItem fu ) {
        if( wallItems == null ) {
            wallItems = new ArrayList<WallItem>();
        }
        wallItems.add( fu );
    }

	@Override
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

    public List<WallItem> getItems() {
        if( wallItems == null ) {
            return Collections.emptyList();
        }
        return wallItems;

    }


}
