package com.bradmcevoy.web.query;

import com.bradmcevoy.web.Formatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This Row class supports aggregation with
 *   - being a list of FieldSource, so it can accumulate other rows
 *   - built in functions to aggregate those rows
 *
 * @author brad
 */
public class Row extends ArrayList<FieldSource> implements FieldSource {

    private Map<String, Object> map = new HashMap<String, Object>();
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
                ss += s + "=" + map.get(s);
            }
            _toString = "Row: " + ss;
        }
        return _toString;
    }

    public Object getData() {
        return this;
    }

    public Object get(String name) {
        return map.get(name);
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public List<FieldSource> getSubRows() {
        return this;
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

    public Set<String> getKeys() {
        return map.keySet();
    }
}
