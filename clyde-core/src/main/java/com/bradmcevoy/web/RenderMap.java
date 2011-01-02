package com.bradmcevoy.web;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This map only exists to support clean templating syntax. Most operations are
 * NOT supported, just get
 *
 * Instead of the old syntax:
 * $rc.invoke("title")
 *
 * Using this we have an equivalent:
 * $include.title
 *
 */
public class RenderMap implements Map<String, Object> {

    private final RenderContext rc;
    private final Boolean componentEdit;

    public RenderMap( RenderContext rc, Boolean componentEdit ) {
        this.rc = rc;
        this.componentEdit = componentEdit;
    }

    @Override
    public Object get( Object key ) {
        return rc.invoke( key.toString(), componentEdit );
    }    


    ///////////////////////////////////////////////////////////////////////
    ///////////           UNSUPPORTED METHODS                   ///////////
    ///////////////////////////////////////////////////////////////////////


    @Override
    public int size() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean containsKey( Object key ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean containsValue( Object value ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Component put( String key, Object value ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Component remove( Object key ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public void putAll( Map<? extends String, ? extends Object> m ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Collection<Object> values() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
