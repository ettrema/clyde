/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ClydeFrame.java
 *
 * Created on 29/01/2011, 10:20:22 AM
 */
package com.bradmcevoy.web.manage.gui;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.web.manage.logging.JTableLogger;
import com.ettrema.event.Event;
import com.ettrema.event.EventListener;
import com.ettrema.event.RequestEvent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author brad
 */
public class ClydeFrame extends javax.swing.JFrame implements EventListener {

    private static ClydeFrame theInstance;
    private final JTableLogger tableLogger;
    private final DefaultTableModel requestModel;

    /** Creates new form ClydeFrame */
    public ClydeFrame() {
        initComponents();
        tableLogger = new JTableLogger( tblLogs );
        this.setVisible( true );
        requestModel = (DefaultTableModel) tblRequests.getModel();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabMain = new javax.swing.JTabbedPane();
        pnlSummary = new javax.swing.JPanel();
        scrollLogs = new javax.swing.JScrollPane();
        tblLogs = new javax.swing.JTable();
        scrollRequests = new javax.swing.JScrollPane();
        tblRequests = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        tabMain.setName("tabMain"); // NOI18N

        pnlSummary.setName("pnlSummary"); // NOI18N

        javax.swing.GroupLayout pnlSummaryLayout = new javax.swing.GroupLayout(pnlSummary);
        pnlSummary.setLayout(pnlSummaryLayout);
        pnlSummaryLayout.setHorizontalGroup(
            pnlSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 395, Short.MAX_VALUE)
        );
        pnlSummaryLayout.setVerticalGroup(
            pnlSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 275, Short.MAX_VALUE)
        );

        tabMain.addTab("Summary", pnlSummary);

        scrollLogs.setName("scrollLogs"); // NOI18N

        tblLogs.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Source", "Priority", "Line", "Message"
            }
        ));
        tblLogs.setName("tblLogs"); // NOI18N
        scrollLogs.setViewportView(tblLogs);

        tabMain.addTab("Logging", scrollLogs);

        scrollRequests.setName("scrollRequests"); // NOI18N

        tblRequests.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Method", "Host", "User", "Path"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblRequests.setName("tblRequests"); // NOI18N
        scrollRequests.setViewportView(tblRequests);

        tabMain.addTab("Requests", scrollRequests);

        getContentPane().add(tabMain, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pnlSummary;
    private javax.swing.JScrollPane scrollLogs;
    private javax.swing.JScrollPane scrollRequests;
    private javax.swing.JTabbedPane tabMain;
    private javax.swing.JTable tblLogs;
    private javax.swing.JTable tblRequests;
    // End of variables declaration//GEN-END:variables

    public JTable getTblLogs() {
        return tblLogs;
    }

    public void onEvent( Event e ) {
        if( e instanceof RequestEvent ) {
            RequestEvent re = (RequestEvent) e;
            logRequest( re.getRequest() );
        }
    }

    private void logRequest( Request request ) {
        Object[] arr = new Object[4];
        arr[0] = request.getMethod();
        arr[1] = request.getHostHeader();
        arr[2] = request.getAbsolutePath();
        if( request.getAuthorization() != null ) {
            arr[3] = request.getAuthorization().getUser();
        } else {
            arr[3] = "";
        }
        requestModel.addRow( arr );
        int newRow = requestModel.getRowCount() - 1;
        requestModel.fireTableRowsInserted( newRow, newRow );
        if( requestModel.getRowCount() > 1000 ) {
            requestModel.removeRow( 0 );
            requestModel.fireTableRowsDeleted( 0, 0 );
        }
    }
}
