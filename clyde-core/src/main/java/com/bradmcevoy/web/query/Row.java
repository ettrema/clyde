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

    public Row(int rowNum) {
        this.rowNum = rowNum;
    }

    public int getRowNum() {
        return rowNum;
    }
        
    @Override
    public String toString() {
        String ss = "";
        for (String s : map.keySet()) {
            ss += s + "=" + map.get(s);
        }
        return "Row: " + ss;
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
        int hash = 7;
        String contents = toString();
        hash = 73 * hash + contents.hashCode();
        return hash;
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
        return toString().equals( other.toString() );
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
