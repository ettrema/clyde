package com.bradmcevoy.web.wall;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author brad
 */
public class FolderUpdateWallItem implements WallItem, Serializable {

    private static final long serialVersionUID = 1L;
    private static int MAX_SIZE = 5;
    private String folderPath;
    private Date lastUpdated;
    private List<UpdatedFile> updatedFiles = new ArrayList<UpdatedFile>();

    public FolderUpdateWallItem( String folderPath ) {
        this.folderPath = folderPath;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public List<UpdatedFile> getUpdatedFiles() {
        return updatedFiles;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void addUpdated( Date modDate, String contentPath, String thumbPath ) {
        this.lastUpdated = modDate;
        if( updatedFiles.size() > MAX_SIZE ) {
            updatedFiles.remove( 0 );
        }
        String thumbHref = null;

        updatedFiles.add( new UpdatedFile( contentPath, thumbHref ) );
    }

    public void pleaseImplementSerializable() {
    }

    public static class UpdatedFile implements Serializable {

        private static final long serialVersionUID = 1L;
        private String href;
        private String thumbHref;

        public UpdatedFile( String href, String thumbHref ) {
            this.href = href;
            this.thumbHref = thumbHref;
        }

        public String getHref() {
            return href;
        }

        public String getThumbHref() {
            return thumbHref;
        }
    }
}
