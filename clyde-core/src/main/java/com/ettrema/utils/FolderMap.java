package com.ettrema.utils;

import com.bradmcevoy.http.Resource;
import com.ettrema.web.Folder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Just provides convenient syntax for template script to navigate file
 * and folder structures
 *
 * @author brad
 */
public class FolderMap implements Map<String,Object> {
    private final Folder folder;

    public FolderMap(Folder folder) {
        this.folder = folder;
    }
        
    @Override
    public Object get(Object key) {
        if("parent".equals(key)) {
            return folder.getParentFolder();
        } else {
            Resource r = folder.child(key.toString());
            if( r == null ) {
                return null;
            } else {
                if( r instanceof Folder ) {
                    return new FolderMap((Folder)r);
                } else {
                    return r;
                }
            }
        }
    }    
    
    @Override
    public int size() {
        return folder.getChildren().size();
    }

    @Override
    public boolean isEmpty() {
        return folder.getChildren().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        Resource r = folder.child(key.toString());
        return r != null;
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<String> keySet() {
        Set<String> set = new HashSet<>();
        for(Resource r : folder.getChildren() ) {
            set.add(r.getName());
        }
        return set;
    }

    @Override
    public Collection<Object> values() {
        List<Object> list = new ArrayList<>();
        for(Resource r : folder.getChildren() ) {
            list.add(r);
        }
        return list;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> set = new HashSet<>();
        for(Resource r : folder.getChildren() ) {
            final String key = r.getName();
            final Object value = r;
            Entry<String, Object> entry = new Entry<String, Object>() {

                @Override
                public String getKey() {
                    return key;
                }

                @Override
                public Object getValue() {
                    return value;
                }

                @Override
                public Object setValue(Object value) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
            set.add(entry);
        }
        return set;
    }
}
