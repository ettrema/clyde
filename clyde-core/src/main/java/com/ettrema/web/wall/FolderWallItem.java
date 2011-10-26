package com.ettrema.web.wall;

import com.ettrema.web.wall.FolderUpdateWallItem.UpdatedFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author brad
 */
public abstract class FolderWallItem implements WallItem, Serializable {

    private static final long serialVersionUID = 1L;
    private String folderPath;
    private Date lastUpdated;
    private List<UpdatedFile> updatedFiles = new ArrayList<UpdatedFile>();

    protected abstract int maxSize();

    public FolderWallItem( String folderPath ) {
        this.folderPath = folderPath;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public List<UpdatedFile> getUpdatedFiles() {
        return updatedFiles;
    }

    @Override
    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated( Date lastUpdated ) {
        this.lastUpdated = lastUpdated;
    }

    

    public void addUpdated( Date modDate, String contentPath, String thumbPath ) {
        this.lastUpdated = modDate;
        if( updatedFiles.size() > maxSize() ) {
            updatedFiles.remove( 0 );
        }
        // If one exists just update it
        for( UpdatedFile uf : updatedFiles ) {
            if( uf.getHref().equals( contentPath ) ) {
                uf.setThumbHref( thumbPath );
                return;
            }
        }
        // add a new one
        updatedFiles.add( new UpdatedFile( contentPath, thumbPath ) );
    }
}
