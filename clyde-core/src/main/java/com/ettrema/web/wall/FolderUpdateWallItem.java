package com.ettrema.web.wall;

import java.io.Serializable;

/**
 *
 * @author brad
 */
public class FolderUpdateWallItem extends  FolderWallItem implements  Serializable {

    private static final long serialVersionUID = 1L;
    private static int MAX_SIZE = 5;

    public FolderUpdateWallItem( String folderPath ) {
        super( folderPath );
    }

    public String getType() {
        return "folder";
    }

    public void pleaseImplementSerializable() {
    }

    @Override
    protected int maxSize() {
        return MAX_SIZE;
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

        public void setThumbHref( String thumbHref ) {
            this.thumbHref = thumbHref;
        }
    }
}
