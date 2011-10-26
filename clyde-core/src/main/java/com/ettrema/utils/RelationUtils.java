package com.ettrema.utils;

import com.bradmcevoy.web.BaseResource;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.Relationship;

/**
 *
 * @author brad
 */
public class RelationUtils {
    public static BaseResource from(Relationship r) {
        if( r == null ) {
            return null;
        }
        NameNode nFrom = r.from();
        if( nFrom == null ) {
            return null;
        }
        DataNode dn = nFrom.getData();
        if( dn == null ) {
            return null;
        } else if( dn instanceof BaseResource) {
            return (BaseResource) dn;
        } else {
            return null;
        }
    }

    public static BaseResource to(Relationship r) {
        if( r == null ) {
            return null;
        }
        NameNode nTo = r.to();
        if( nTo == null ) {
            return null;
        }
        DataNode dn = nTo.getData();
        if( dn == null ) {
            return null;
        } else if( dn instanceof BaseResource) {
            return (BaseResource) dn;
        } else {
            return null;
        }
    }
}
