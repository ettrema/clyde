package com.bradmcevoy.web.image;

import java.io.IOException;
import java.io.InputStream;
import org.apache.sanselan.*;

/**
 *
 * @author brad
 */
public class ImageService {

    public Dimensions getDimenions( InputStream in, String name ) {
        try {
            ImageInfo info = Sanselan.getImageInfo( in, name );
            info.getHeight();
            info.getWidth();
            return new Dimensions( info.getWidth(), info.getHeight());
        } catch( ImageReadException ex ) {
            throw new RuntimeException( ex );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
    }

//    public void getExifData(InputStream in, String name) {
//        try {
//            IImageMetadata meta = Sanselan.getMetadata( in, name );
//            if( meta instanceof JpegImageMetadata) {
//                JpegImageMetadata jpegMeta = (JpegImageMetadata) meta;
//                jpegMeta.getExif().
//            } else {
//
//            }
//        } catch( ImageReadException ex ) {
//            throw new RuntimeException( ex );
//        } catch( IOException ex ) {
//            throw new RuntimeException( ex );
//        }
//
//    }
}
