package com.bradmcevoy.web.wall;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author brad
 */
public class FileTrashedWallItem implements WallItem, Serializable {

    private static final long serialVersionUID = 1L;

    private String filePath;
    private Date lastUpdated;

    public FileTrashedWallItem( String filePath ) {
        this.filePath = filePath;
    }

    public String getType() {
        return "trashed";
    }

    public String getFilePath() {
        return filePath;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated( Date lastUpdated ) {
        this.lastUpdated = lastUpdated;
    }

    public void pleaseImplementSerializable() {
        
    }

}
