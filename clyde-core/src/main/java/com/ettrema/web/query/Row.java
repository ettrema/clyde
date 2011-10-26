package com.ettrema.web.query;

import com.ettrema.web.Formatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This Row class supports aggregation with
 *   - being a list of FieldSource, so it can accumulate other rows
 *   - built in functions to aggregate those rows
 *
 * @author brad
 */
public class Row implements FieldSource, Map<String, Object>, Iterable<FieldSource> {

	private Map<String, Object> map = new HashMap<String, Object>();
	private List<FieldSource> rows = new ArrayList<FieldSource>();
	private final int rowNum;
	private String _toString;
	private int _hash;

	public Row(int rowNum) {
		this.rowNum = rowNum;
	}

	public int getRowNum() {
		return rowNum;
	}

	/**
	 * IMPORTANT: this is only calculated once! Is held for performance
	 *
	 * @return
	 */
	@Override
	public String toString() {
		if (_toString == null) {
			String ss = "";
			for (String s : map.keySet()) {
				ss += s + "=" + map.get(s) + "|";
			}
			_toString = "Row: " + ss;
		}
		return _toString;
	}

	@Override
	public Object getData() {
		return this;
	}

	@Override
	public Object get(String name) {
		return map.get(name);
	}

	public Map<String, Object> getMap() {
		return map;
	}

	public List<FieldSource> getSubRows() {
		return rows;
	}

	@Override
	public int hashCode() {
		if (_hash == 0) {
			int hash = 7;
			String contents = toString();
			hash = 73 * hash + contents.hashCode();
			_hash = hash;
		}
		return _hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Row other = (Row) obj;
		return toString().equals(other.toString());
	}

	public long sum(String name) {
		if (isEmpty()) {
			return 0;
		}
		long cnt = 0;
		for (FieldSource row : this) {
			Object o = row.get(name);
			Long l = Formatter.getInstance().toLong(o);
			cnt += l;
		}
		return cnt;
	}

	public Long average(String name) {
		long cnt = getCount();
		if (cnt == 0) {
			return null;
		} else {
			return sum(name) / cnt;
		}
	}

	public long getCount() {
		return size();
	}

	@Override
	public Set<String> getKeys() {
		return map.keySet();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public Object get(Object key) {
		return map.get(key);
	}

	@Override
	public Object put(String key, Object value) {
		return map.put(key, value);
	}

	@Override
	public Object remove(Object key) {
		return map.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		map.putAll(m);
	}

	@Override
	public Set<String> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<Object> values() {
		return map.values();
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return map.entrySet();
	}

	/**
	 * NOTE: there is a semantics clash here. Since a Row represents both a
	 * list (ie of aggregated subrows) and a map (of fields for this row) there
	 * is a different interpretation of size for each.
	 * 
	 * It seems that size is most relevant to the list, so it is returned here
	 * 
	 * @return 
	 */
	@Override
	public int size() {
		return rows.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Iterator<FieldSource> iterator() {
		return rows.iterator();
	}
}
