package com.bradmcevoy.web.query;

import com.bradmcevoy.web.Formatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author brad
 */
public class Row implements FieldSource {

    private Map<String, Object> map = new HashMap<String, Object>();
    private List<FieldSource> subRows;

    @Override
    public String toString() {
        String ss = "";
        for (String s : map.keySet()) {
            ss += s + "=" + map.get(s);
        }
        return "Row: " + ss;
    }

    public Object getData() {
        return subRows;
    }

    public Object get(String name) {
        return map.get(name);
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public List<FieldSource> getSubRows() {
        if (subRows == null) {
            subRows = new AggregatingList();
        }
        return subRows;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.map != null ? this.map.hashCode() : 0);
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
        if (this.map != other.map && (this.map == null || !this.map.equals(other.map))) {
            return false;
        }
        return true;
    }

    public class AggregatingList extends ArrayList<FieldSource> {

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
    }
}
