package com.bradmcevoy.facebook;

import com.bradmcevoy.web.ImageFile;

/**
 *
 * @author brad
 */
public interface FaceBookGalleryManager {
    /**
     * creates the album if it doesnt exist
     *
     * @param galleryName
     * @param credentials
     * @return - the album id
     */
    Long checkOrCreateAlbum(String galleryName, FaceBookCredentials credentials);

    void loadImageToAlbum( ImageFile img, Long albumId, FaceBookCredentials cred, String caption );

}
