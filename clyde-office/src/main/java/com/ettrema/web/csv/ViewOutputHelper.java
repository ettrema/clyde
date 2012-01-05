package com.ettrema.web.csv;

import au.com.bytecode.opencsv.CSVWriter;
import com.ettrema.web.Formatter;
import com.ettrema.web.query.Field;
import com.ettrema.web.query.FieldSource;
import com.ettrema.web.query.Selectable;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class ViewOutputHelper {

    private final Formatter formatter = Formatter.getInstance();
    private final static ViewOutputHelper theInstance = new ViewOutputHelper();

    public static ViewOutputHelper getInstance() {
        return theInstance;
    }

    public void toCsv(OutputStream out, Selectable selectable, List<FieldSource> rows) {
        PrintWriter pw = new PrintWriter(out);
        CSVWriter writer = new CSVWriter(pw);

        // write header row
        List<String> values = selectable.getFieldNames();
        String[] arr = new String[values.size()];
        values.toArray(arr);
        writer.writeNext(arr);

        // write the data
        toCsv(rows, writer, selectable);

        // clean up
        pw.flush();
        pw.close();

    }

    public void toCsv(OutputStream out, Iterable<ViewRecord> records) {
        PrintWriter pw = new PrintWriter(out);
        CSVWriter writer = new CSVWriter(pw);
        toCsv(records, writer);
        pw.flush();
        pw.close();

    }

    private void toCsv(List<FieldSource> rows, CSVWriter writer, Selectable selectable) {
        for (FieldSource row : rows) {
            List<String> values;
            values = buildLineOfValues(row, selectable);
            String[] arr = new String[values.size()];
            values.toArray(arr);
            writer.writeNext(arr);
        }

    }

    private void toCsv(Iterable<ViewRecord> records, CSVWriter writer) {
        List<String> values;
        for (ViewRecord rec : records) {
            if (rec.getSelect().getSubSelect() == null) {
                values = buildLineOfValues(rec);
                String[] arr = new String[values.size()];
                values.toArray(arr);
                writer.writeNext(arr);
            } else {
                toCsv(rec.getChildren(), writer);
            }
        }
    }

    private List<String> buildLineOfValues(ViewRecord rec) {
        List<String> values;
        if (rec.getParent() != null) {
            values = buildLineOfValues(rec.getParent());
        } else {
            values = new ArrayList<String>();
            return values; // since this is the root record, don't add any fields
        }
        values.add(rec.getRes().getName());
        for (Field f : rec.getSelect().getFields()) {
            Object val = rec.getRes().value(f.getName());
            values.add(formatter.format(val));
        }
        return values;
    }

    private List<String> buildLineOfValues(FieldSource row, Selectable selectable) {
        List<String> values = new ArrayList<String>();
        for (String key : selectable.getFieldNames()) {
            Object val = row.get(key);
            values.add(formatter.format(val));
        }
        return values;
    }
}
