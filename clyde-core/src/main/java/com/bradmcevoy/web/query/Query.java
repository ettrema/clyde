package com.bradmcevoy.web.query;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Formatter;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.eval.Evaluatable;
import com.bradmcevoy.web.query.OrderByField.Direction;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A query has a list of fields to be evaluated for the rows in a source, where
 * the source might be another query or a folder path.
 *
 * There is an optional where object which will be evaluated for each row, and
 * only rows with a true value are output.
 *
 *
 * @author brad
 */
public class Query implements Selectable, Evaluatable, Serializable, Comparator<FieldSource> {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Query.class);
    private static final long serialVersionUID = 1L;
    private List<Field> selectFields;
    private Map<String, Field> groupFields;
    private Selectable from;
    private Evaluatable where;
    private List<OrderByField> orderByFields;

	@Override
    public List<String> getFieldNames() {
        ArrayList<String> list = new ArrayList<String>();
        for (Field f : selectFields) {
            list.add(f.getName());
        }
        return list;
    }

    public List<Field> getSelectFields() {
        return selectFields;
    }

    public void setSelectFields(List<Field> selectFields) {
        this.selectFields = selectFields;
    }

    public List<OrderByField> getOrderByFields() {
        return orderByFields;
    }

    public void setOrderByFields(List<OrderByField> orderByFields) {
        this.orderByFields = orderByFields;
    }

    public Map<String, Field> getGroupFields() {
        return groupFields;
    }

    public void setGroupFields(Map<String, Field> groupFields) {
        this.groupFields = groupFields;
    }

    public Selectable getFrom() {
        return from;
    }

    public void setFrom(Selectable from) {
        this.from = from;
    }

    public Evaluatable getWhere() {
        return where;
    }

    public void setWhere(Evaluatable where) {
        this.where = where;
    }

	@Override
    public Object evaluate(RenderContext rc, Addressable from) {
		log.info("evaluate query");
        CommonTemplated relativeTo = (CommonTemplated) from;
        if (!(relativeTo instanceof Folder)) {
            relativeTo = relativeTo.getParentFolder();

        }
		List<FieldSource> list = getRows((Folder) relativeTo);
		log.trace("evaluate returned rows: " + list.size());
		return list;
    }

	@Override
    public Object evaluate(Object from) {
        Folder relativeTo = (Folder) from;
        List<FieldSource> list =  getRows(relativeTo);
		log.trace("evaluate returned rows: " + list.size());
		return list;
    }

	@Override
    public List<FieldSource> getRows(Folder relativeTo) {
        long t = System.currentTimeMillis();
        List<FieldSource> sourceRows;
		
        try {
            log.trace("getRows: " + relativeTo.getHref());
            if (groupFields == null || groupFields.isEmpty()) {
                log.trace("non aggregating query");
                List<FieldSource> output = new ArrayList<FieldSource>();
                sourceRows = from.getRows(relativeTo);
                log.trace("got source rows: " + sourceRows.size() + "  in " + (System.currentTimeMillis() - t) + "ms");
                for (FieldSource fs : sourceRows) {
                    if (isWhereTrue(fs)) {
                        Row row = buildRow(fs, selectFields, output.size());
                        output.add(row);
                    }
                }
                log.trace("done non aggregating query in: " + (System.currentTimeMillis() - t) + "ms  rows: " + output.size());
                return sort(output);
            } else {
                log.trace("aggregating query");
                Map<Row, Row> output = new HashMap<Row, Row>();
				long tt = System.currentTimeMillis();
				List<FieldSource> fromRows = from.getRows(relativeTo);
				tt = System.currentTimeMillis() - tt;
				log.trace("got fromRows: " + fromRows.size() + " in " + tt + "ms");
                for (FieldSource fs : fromRows) {
                    if (isWhereTrue(fs)) {
                        addAggregatedResult(fs, output);
                    }
                }
                calcAggegatedFields(output);
                return sort(output.values());
            }
        } finally {
            t = System.currentTimeMillis() - t;
            if (t > 15000) {
                log.warn("Very slow query: " + t + "ms");
            } else if (t > 1500) {
                log.info("Slow query: " + t + "ms");
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("query time: " + t + "ms");
                }
            }
        }
    }
	
	@Override
	public long processRows(Folder relativeTo, RowProcessor rowProcessor) {
        long t = System.currentTimeMillis();
        List<FieldSource> sourceRows;
		long count = 0;
        try {
            log.trace("getRows: " + relativeTo.getHref());
            if (groupFields == null || groupFields.isEmpty()) {
                log.trace("non aggregating query");
                sourceRows = from.getRows(relativeTo);
                log.trace("got source rows: " + sourceRows.size() + "  in " + (System.currentTimeMillis() - t) + "ms");
				int rowNum = 0;
                for (FieldSource fs : sourceRows) {
                    if (isWhereTrue(fs)) {
                        Row row = buildRow(fs, selectFields, rowNum++);
						count++;
                        rowProcessor.process(row);
                    }
                }
                log.trace("done non aggregating process query in: " + (System.currentTimeMillis() - t) + "ms  rows: " + rowNum);
            } else {
                log.trace("aggregating query");
                Map<Row, Row> output = new HashMap<Row, Row>();
				long tt = System.currentTimeMillis();
				List<FieldSource> fromRows = from.getRows(relativeTo);
				tt = System.currentTimeMillis() - tt;
				log.trace("got fromRows: " + fromRows.size() + " in " + tt + "ms");
                for (FieldSource fs : fromRows) {
                    if (isWhereTrue(fs)) {
                        addAggregatedResult(fs, output);
                    }
                }
                calcAggegatedFields(output);
				for( Row row : output.values()) {
					count++;
					rowProcessor.process(row);
				}
            }
			return count;
        } finally {
            t = System.currentTimeMillis() - t;
            if (t > 15000) {
                log.warn("Very slow query: " + t + "ms");
            } else if (t > 1500) {
                log.info("Slow query: " + t + "ms");
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("query time: " + t + "ms");
                }
            }
        }
	}	

    private boolean isWhereTrue(FieldSource fs) {
        if (where == null) {
            return true;
        } else {
            Object oVal = where.evaluate(fs.getData());
            Boolean bb = Formatter.getInstance().toBool(oVal);
            if (bb == null) {
                return true;
            } else {
                return bb.booleanValue();
            }
        }
    }

    /**
     * For groupby queries, we accumulate sub-rows within the group by rows,
     * then we'll apply field expressions at the end
     *
     * @param fs
     * @param output
     */
    private void addAggregatedResult(FieldSource fs, Map<Row, Row> output) {
        Row groupByKey = buildRow(fs, groupFields.values(), -1);
        Row outputRow = output.get(groupByKey);
        if (outputRow == null) {
            outputRow = new Row(output.size());
            output.put(groupByKey, outputRow);
        }
        // Note that fields are calculated for each row in a second phase, after rows are accumulated
        outputRow.getSubRows().add(fs);
    }

    private Row buildRow(FieldSource fs, Collection<Field> fields, int rowNum) {
        Row row = new Row(rowNum);
        populateRow(row, fs, fields);
        return row;
    }

    private Object evaluate(Field f, FieldSource fs) {
        try {
            if (f.getEvaluatable() == null) {
                Object o = fs.get(f.getName());
                return o;
            } else {
                //return f.getEvaluatable().evaluate(fs);
                return f.getEvaluatable().evaluate(fs.getData());
            }
        } catch (Exception e) {
            log.error("Exception in evaluate. field: " + f.getName() + " from fieldSource: " + fs.toString(), e);
            return "ERR";
        }
    }

    private void calcAggegatedFields(Map<Row, Row> output) {
        log.trace("calcAggegatedFields");
		long t = System.currentTimeMillis();
        for (Row keyRow : output.keySet()) {
            Row valueRow = output.get(keyRow);
            for (Field f : selectFields) {
                if (groupFields.containsKey(f.getName())) {
                    Object groupByValue = keyRow.get(f.getName());
                    valueRow.getMap().put(f.getName(), groupByValue);
                } else {
                    Object o = evaluate(f, valueRow);
                    valueRow.getMap().put(f.getName(), o);
                }
            }
        }
		t = System.currentTimeMillis() - t;
		log.trace("calcAggegatedFields: done aggregation in " + t + "ms");
    }

    private void populateRow(Row row, FieldSource fs, Collection<Field> fields) {
        for (Field f : fields) {
            Object o = evaluate(f, fs);
            row.getMap().put(f.getName(), o);
        }
    }

	@Override
    public void pleaseImplementSerializable() {
    }

    private List<FieldSource> sort(List<FieldSource> output) {
        long t = System.currentTimeMillis();
        log.trace("sort...");
        Collections.sort(output, this);
        log.trace("sorted: rows: " + output.size() + " in " + (System.currentTimeMillis() - t) + "ms");
        return output;
    }

    private List<FieldSource> sort(Collection<Row> values) {
        List<FieldSource> list = new ArrayList<FieldSource>();
        list.addAll(values);
        List<FieldSource> sorted = sort(list);
		return sorted;
    }

	@Override
    public int compare(FieldSource o1, FieldSource o2) {
        if (orderByFields == null || orderByFields.isEmpty()) {
            if (o1.hashCode() < o2.hashCode()) {
                return -1;
            } else if (o1.hashCode() > o2.hashCode()) {
                return 1;
            } else {
                return 0;
            }
        }
        for (OrderByField f : orderByFields) {
            Object val1 = evaluateOrderBy(f, o1);
            Object val2 = evaluateOrderBy(f, o2);
            int c = compareValues(val1, val2);
            if (c != 0) {
                if (f.getDirection().equals(Direction.ascending)) {
                    return c;
                } else {
                    return c * -1;
                }
            }
        }
        // The fieldsource's are identical, sorting-wise
        return 0;
    }

    private Object evaluateOrderBy(Field f, FieldSource row) {
        if (f.getEvaluatable() == null) {
            return row.get(f.getName());
        } else {
            return evaluate(f, row);
        }
    }

    private int compareValues(Object val1, Object val2) {
        if (val1 instanceof Comparable) {
            if (val2 instanceof Comparable) {
                Comparable c1 = (Comparable) val1;
                Comparable c2 = (Comparable) val2;
                return c1.compareTo(c2);
            }
        }
        // Values arent directly comparable, so convert to string and compare
        String s1 = Formatter.getInstance().toString(val1);
        String s2 = Formatter.getInstance().toString(val2);
        return s1.compareTo(s2);
    }

}
