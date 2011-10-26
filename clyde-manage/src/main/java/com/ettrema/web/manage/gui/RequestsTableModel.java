package com.bradmcevoy.web.manage.gui;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Response;
import com.ettrema.event.Event;
import com.ettrema.event.EventListener;
import com.ettrema.event.EventManager;
import com.ettrema.event.RequestEvent;
import com.ettrema.event.ResponseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author brad
 */
public class RequestsTableModel extends AbstractTableModel implements EventListener {

    private List<Item> requests;
    private Map<Request,Item> mapOfItems;

    public RequestsTableModel(EventManager eventManager) {
        requests = new ArrayList<Item>();
        mapOfItems = new HashMap<Request, Item>();
        eventManager.registerEventListener( this, RequestEvent.class );
        eventManager.registerEventListener( this, ResponseEvent.class );
    }

    public Request getRequest(int row) {
        return requests.get(row).request;
    }

    public Response getResponse(int row) {
        return requests.get(row).response;
    }

    public int getRowCount() {
        return requests.size();
    }

    public int getColumnCount() {
        return 5;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Item item = requests.get(rowIndex);
        Request request = item.request;
        Response response = item.response;
        switch(columnIndex) {
            case 0:
                return request.getMethod().code;
            case 1:
                return request.getHostHeader();
            case 2:
                if( request.getAuthorization() != null ) {
                    return request.getAuthorization().getUser();
                } else {
                    return "";
                }
            case 3:
                return request.getAbsolutePath();
            case 4:
                if( response == null ) {
                    return "";
                } else {
                    return response.getStatus();
                }
            default:
                throw new RuntimeException("no such column: " + columnIndex);

        }

    }



    public void onEvent( Event e ) {
        if( e instanceof RequestEvent ) {
            RequestEvent re = (RequestEvent) e;
            logRequest( re.getRequest() );
        } else if( e instanceof ResponseEvent) {
            ResponseEvent re = (ResponseEvent) e;
            logResponse(re.getRequest(), re.getResponse());
        }
    }

    private void logRequest( Request request ) {
        Item item = new Item();
        item.request = request;
        requests.add(item);
        mapOfItems.put(request, item);
        if( requests.size() > 100 ) {
            Item toRemove = requests.get(0);
            mapOfItems.remove(toRemove.request);
            requests.remove( 0 );
        }
        fireTableDataChanged();
    }

    private void logResponse(Request request, Response response) {        
        Item item = mapOfItems.get(request);
        if( item == null ) {
            return;
        } else {
            item.response = response;
            int row = requests.indexOf(item);
            fireTableRowsUpdated(row, row);
        }
    }

    void clearAll() {
        requests.clear();
        mapOfItems.clear();
        fireTableDataChanged();
    }

    private class Item {
        Request request;
        Response response;
    }
}
