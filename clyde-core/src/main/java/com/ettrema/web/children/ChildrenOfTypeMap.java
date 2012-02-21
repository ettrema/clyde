package com.ettrema.web.children;

import com.ettrema.web.BaseResourceList;
import com.ettrema.web.Templatable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author brad
 */
public class ChildrenOfTypeMap implements Map<String,BaseResourceList>{

    private final BaseResourceList list;

    public ChildrenOfTypeMap(BaseResourceList list) {
        this.list = list;
    }
    
    
    
    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return true;
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BaseResourceList get(Object key) {
        return list.ofType(key.toString());               
    }

    @Override
    public BaseResourceList put(String key, BaseResourceList value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BaseResourceList remove(Object key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putAll(Map<? extends String, ? extends BaseResourceList> m) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<BaseResourceList> values() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Entry<String, BaseResourceList>> entrySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
