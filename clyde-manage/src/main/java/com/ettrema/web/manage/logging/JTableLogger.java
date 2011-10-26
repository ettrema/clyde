package com.bradmcevoy.web.manage.logging;

import com.ettrema.logging.NotifyingAppender;
import java.awt.Rectangle;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author brad
 */
public final class JTableLogger implements com.ettrema.logging.NotifyingAppender.Listener {

    private final JTable table;
    private final DefaultTableModel model;
    private int maxLength = 5000;

    public JTableLogger(JTable t) {
        table = t;
        model = (DefaultTableModel) t.getModel();
        NotifyingAppender.addListener(this);
    }

    public void onEvent(LoggingEvent event) {
        try {
            while (model.getRowCount() >= maxLength) {
                model.removeRow(0);
                model.fireTableRowsDeleted(0, 0);
            }
            Object[] row = new Object[4];
            row[0] = event.getLevel().toString();
            row[1] = event.getLoggerName() + "::" + event.getLocationInformation().getMethodName() + ":" + event.getLocationInformation().getLineNumber();
            row[2] = event.getMessage();
            model.addRow(row);
            int newRow = model.getRowCount() - 1;
            model.fireTableRowsInserted(newRow, newRow);
            Rectangle rect = table.getCellRect(newRow, 0, true);
            table.scrollRectToVisible(rect);
        } catch (Throwable e) {
            // hack alert! sometimes get NPE's. Rather then fix it i'll just catch the exception, like all good programmers do...
            e.printStackTrace();
        }
    }

    public void clearAll() {
        model.setRowCount(0);
        model.fireTableDataChanged();
    }
}
