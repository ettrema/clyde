package com.bradmcevoy.media;

import com.bradmcevoy.io.FileUtils;
import java.io.InputStream;
import com.bradmcevoy.web.BaseResource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.image.ImageService;
import java.awt.image.BufferedImage;
import com.bradmcevoy.web.ImageFile;
import com.bradmcevoy.web.Thumb;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;



/**
 *
 * @author brad
 */
public class ImageServiceThumbProcessor implements ThumbProcessor{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ImageServiceThumbProcessor.class );

    private final ImageService imageService;

    public ImageServiceThumbProcessor(ImageService imageService) {
        this.imageService = imageService;
    }



    public int generateThumbs( ImageFile imageFile, List<Thumb> thumbs, boolean skipIfExists ) throws FileNotFoundException, IOException {
        return generateThumbs( imageFile, thumbs, Rotate.NONE, skipIfExists );
    }

    public int generateThumbs( ImageFile imageFile, List<Thumb> thumbs, Rotate rotate, boolean skipIfExists ) throws FileNotFoundException, IOException {
        log.trace( "generateThumbs: " + skipIfExists );

        if( thumbs == null ) {
            log.trace( "no thumbs" );
            return 0;
        }
        if( imageFile.isTrash() ) {
            log.debug( "in trash, not generating: : " + imageFile.getPath() );
            return 0;
        }

        if( log.isTraceEnabled() ) {
            log.trace( "doing thumb processing with thumbs: " + Thumb.format( thumbs ) );
        }

        String format = FileUtils.getExtension(imageFile.getName());
        BufferedImage image = imageService.read( imageFile.getInputStream(), format );
        if( image == null ) {
            log.warn("Couldnt get an image for: " + imageFile.getPath());
            return 0;
        }

        switch(rotate) {
            case NONE:
                break;
            case LEFT:
                image = imageService.rotateLeft( image );
                break;
            case RIGHT:
                image = imageService.rotateRight( image );
                break;
        }


        Folder parent = imageFile.getParent();
        int count = 0;
        for( Thumb t : thumbs ) {
            if( log.isTraceEnabled() ) {
                log.trace( "gen thumb: " + t.toString() );
            }
            Folder thumbsFolder = parent.thumbs( t.getSuffix(), true );
            // Ensure we dont do versioning of thumbs
            if( thumbsFolder.isVersioningEnabled() == null ) {
                thumbsFolder.setVersioningEnabled( false );
                thumbsFolder.save();
            }
            createThumb(  imageFile, thumbsFolder, t.getWidth(), t.getHeight(), skipIfExists, image );
            count++;
        }
        log.trace( "generated: " + count );
        return count;
    }

    private void createThumb( ImageFile imageFile, Folder folder, int width, int height, boolean skipIfExists, BufferedImage image ) {
        log.trace("createThumb");
        ByteArrayOutputStream out = new ByteArrayOutputStream( 50000 );
        try {
            imageService.scaleProportionallyWithMax( image, out, height, width, "jpeg" );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
        byte[] thumbData = out.toByteArray();
        if( thumbData.length == 0 ) {
            throw new RuntimeException( "Generated a zero size thumb nail: " + imageFile.getPath() );
        }

        BaseResource resExisting = folder.childRes( imageFile.getName() );
        if( resExisting != null ) {
            if( skipIfExists ) {
                return;
            } else {
                resExisting.deletePhysically();
            }
        }
        ImageFile thumb = new ImageFile( "image/jpeg", folder, imageFile.getName() );
        log.debug( "create thumb: " + thumb.getHref() );
        thumb.save();
        InputStream in = new ByteArrayInputStream( thumbData );
        thumb.setContent( in );

    }


}
