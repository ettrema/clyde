package com.ettrema.web.query;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Just wraps around a FieldSource to make it a map
 *
 * @author bradm
 */
public class FieldSourceMap implements FieldSource, Map<String,Object> {

	private final FieldSource fieldSource;

	public FieldSourceMap(FieldSource fieldSource) {
		this.fieldSource = fieldSource;
	}
	
	
	
	@Override
	public Object get(String name) {
		return fieldSource.get(name);
	}

	@Override
	public Object getData() {
		return fieldSource.getData();
	}

	@Override
	public Set<String> getKeys() {
		return fieldSource.getKeys();
	}

	@Override
	public int size() {
		return fieldSource.getKeys().size();
	}

	@Override
	public boolean isEmpty() {
		return fieldSource.getKeys().isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return fieldSource.getKeys().contains(key);
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Object get(Object key) {
		return fieldSource.get(key.toString());
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
		return fieldSource.getKeys();
	}

	@Override
	public Collection<Object> values() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
}
