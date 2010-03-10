package com.bradmcevoy.facebook;

import com.bradmcevoy.web.ImageFile;
import com.google.code.facebookapi.FacebookException;
import com.google.code.facebookapi.FacebookXmlRestClient;
import com.google.code.facebookapi.schema.Album;
import com.google.code.facebookapi.schema.PhotosGetAlbumsResponse;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBElement;

/**
 * Facebook Photo Upload Error Codes
 * 1	 An unknown error occurred. Please resubmit the request.
2	The service is not available at this time.
5	The request came from a remote address not allowed by this application.
100	One of the parameters specified was missing or invalid.
101	The API key submitted is not associated with any known application.
102	The session key was improperly submitted or has reached its timeout. Direct the user to log in again to obtain another key.
103	The submitted call_id was not greater than the previous call_id for this session.
104	Incorrect signature.
120	Invalid album ID.
200	The application does not have permission to operate on the passed in uid parameter.
321	Album is full.
324	Missing or invalid image file.
325	Too many unapproved photos pending.
 *
 * @author brad
 */
public class FaceBookGalleryManagerImpl implements FaceBookGalleryManager {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( FaceBookGalleryManagerImpl.class );

    static {
        FacebookXmlRestClient.initJaxbSupport();
    }

    /**
     *
     * @param galleryPath
     * @param cred
     * @return - the album id
     */
    public Long checkOrCreateAlbum( String galleryName, FaceBookCredentials cred ) {
        log.debug( "checkOrCreateAlbum: " + galleryName );
        FacebookXmlRestClient userClient = new FacebookXmlRestClient( cred.getApiKey(), cred.getApiSecret(), cred.getSessionId() );
        try {
            userClient.photos_getAlbums( cred.getUserId() );
            PhotosGetAlbumsResponse resp = (PhotosGetAlbumsResponse) userClient.getResponsePOJO();
            if( resp.getAlbum() != null && resp.getAlbum().size() > 0 ) {
                for( Album album : resp.getAlbum() ) {
                    if( album.getName().equals( galleryName ) ) { // todo: concat path to get name
                        // exists\
                        log.debug( "found: " + album.getName() );
                        String s = album.getAid();
                        return Long.parseLong( s );
                    }
                }
                log.debug( "album not found: " + galleryName );
            } else {
                log.debug( "no albums found" );
            }

            userClient.photos_createAlbum( galleryName ); // todo: concat path to get name
            // todo: concat path to get name
            Object o = userClient.getResponsePOJO();
            log.debug( "got a : " + o.getClass() );
            JAXBElement jaxb = (JAXBElement) o;
            Album createResp = (Album) jaxb.getValue();
            String s = createResp.getAid();
            return Long.parseLong( s );
        } catch( FacebookException ex ) {
            throw new RuntimeException( ex );
        }
    }

    public void loadImageToAlbum( ImageFile img, Long albumId, FaceBookCredentials cred, String caption ) {
        log.debug( "loadImageToAlbum: " + img.getPath() );
        FacebookXmlRestClient userClient = new FacebookXmlRestClient( cred.getApiKey(), cred.getApiSecret(), cred.getSessionId() );
        InputStream in = null;
        try {
            in = img.getInputStream();
            userClient.photos_upload( cred.getUserId(), caption, albumId, img.getName(), in );
        } catch( FacebookException ex ) {
            throw new RuntimeException( ex );
        } finally {
            close( in );
        }
    }

//    public List<String> getPhotoNames( Long albumId, FaceBookCredentials cred ) {
//        try {
//            FacebookXmlRestClient userClient = new FacebookXmlRestClient( cred.getApiKey(), cred.getApiSecret(), cred.getSessionId() );
//            userClient.photos_getByAlbum( albumId );
//            Object o = userClient.getResponsePOJO();
//            log.debug( "got a : " + o.getClass() );
//            if( o instanceof JAXBElement) {
//                JAXBElement jaxb = (JAXBElement) o;
//                o = jaxb.getValue();
//            }
//            PhotosGetResponse resp = (PhotosGetResponse) o;
//            List<String> photoNames = new ArrayList<String>();
//            for( Photo respPhoto : resp.getPhoto() ) {
//                photoNames.add( respPhoto.getSrc());
//            }
//            return photoNames;
//        } catch( FacebookException ex ) {
//            throw new RuntimeException( ex );
//        }
//
//    }

    private void close( InputStream in ) {
        if( in != null ) try {
                in.close();
            } catch( IOException ex ) {
            }
    }
}
