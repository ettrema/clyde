
package com.ettrema.web.stats;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Response;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import java.util.Date;
import java.util.UUID;

public class AccessRecord implements DataNode{

    private UUID id;
    private NameNode nameNode;
            
    private final Date date;
    private final String host;
    private final String path;
    private final String fromAddress;
    private final String method;
    private final String responseCode;
    private final long duration;

    public AccessRecord(Request request, Response response, long duration) {
        this.date = new Date();
        this.host = request.getHostHeader();
        this.path = request.getAbsolutePath();
        this.fromAddress = request.getFromAddress();
        this.method = request.getMethod().code;
        if( response.getStatus() != null ) {
            this.responseCode = response.getStatus().toString();
        } else {
            this.responseCode = "";
        }
        this.duration = duration;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void init(NameNode nameNode) {
        this.nameNode = nameNode;
    }

    public void onDeleted(NameNode nameNode) {
    }

    public String getHost() {
        return host;
    }


    public String getPath() {
        return path;
    }


    public String getFromAddress() {
        return fromAddress;
    }


    public String getMethod() {
        return method;
    }


    public String getResponseCode() {
        return responseCode;
    }


    public long getDuration() {
        return duration;
    }

    public Date getDate() {
        return date;
    }
    
    
    @Override
    public String toString() {
        return host + ":" + path + "," + fromAddress + "(" + duration + ") - " + responseCode;
    }

     
}
