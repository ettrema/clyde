package com.ettrema.web;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author brad
 */
public class Relations implements Map<String, BaseResource> {

    BaseResource baseResource;

    public Relations(BaseResource baseResource) {
        this.baseResource = baseResource;
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof String) {
            BaseResource c = get((String) key);
            return c != null;
        } else {
            return false;
        }
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BaseResource get(Object key) {
        BaseResource rel = baseResource.getRelation(key.toString());
        return rel;
    }

    @Override
    public BaseResource put(String key, BaseResource value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BaseResource remove(Object key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putAll(Map<? extends String, ? extends BaseResource> m) {
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
    public Collection<BaseResource> values() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Entry<String, BaseResource>> entrySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
