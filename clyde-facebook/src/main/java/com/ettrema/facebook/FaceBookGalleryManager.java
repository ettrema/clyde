package com.ettrema.facebook;

import com.ettrema.web.ImageFile;

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
    String checkOrCreateAlbum(String galleryName, FaceBookCredentials credentials);

    void loadImageToAlbum( ImageFile img, String albumId, FaceBookCredentials cred, String caption );

}
