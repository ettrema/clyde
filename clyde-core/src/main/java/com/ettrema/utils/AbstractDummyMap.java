package com.ettrema.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Serves as a base class for maps which are just templating convenience
 *
 * @author brad
 */
public abstract class AbstractDummyMap<K,T> implements Map<K,T> {

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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public T put(K key, T value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public T remove(Object key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putAll(Map<? extends K, ? extends T> m) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<T> values() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Entry<K, T>> entrySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
