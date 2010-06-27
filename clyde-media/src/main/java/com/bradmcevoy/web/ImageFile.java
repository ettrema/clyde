package com.bradmcevoy.web;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.property.BeanPropertyResource;
import com.bradmcevoy.utils.FileUtils;
import com.bradmcevoy.web.image.Dimensions;
import com.bradmcevoy.web.image.ImageService;
import com.bradmcevoy.web.image.ImageUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@BeanPropertyResource("clyde")
public class ImageFile extends BinaryFile {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ImageFile.class );
    private static final long serialVersionUID = 1L;

    private ImageData imageData;

    public ImageFile( String contentType, Folder parentFolder, String newName ) {
        super( contentType, parentFolder, newName );
    }

    public ImageFile( Folder parentFolder, String newName ) {
        super( "image", parentFolder, newName );
    }

    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new ImageFile( parent, newName );
    }

    @Override
    public boolean is( String type ) {
        if( "image".equalsIgnoreCase( type ) ) return true;
        return super.is( type );
    }

    @Override
    protected void afterSetContent() {
        super.afterSetContent();
//        this.generateThumbs();
    }

    public void createThumb( Folder folder, int width, int height ) {
        createThumb( folder, width, height, false );
    }

    public void createThumb( Folder folder, int width, int height, boolean skipIfExists ) {
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream( 50000 );
        try {
            in = getInputStream();
            ImageUtilities.scaleProportionallyWithMax( in, out, height, width, "jpeg" );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        } finally {
            FileUtils.close( in );
        }

        BaseResource resExisting = folder.childRes( this.getName() );
        if( resExisting != null ) {
            if( skipIfExists ) {
                return;
            } else {
                resExisting.deletePhysically();
            }
        }
        ImageFile thumb = new ImageFile( "image/jpeg", folder, this.getName() );
        log.debug( "create thumb: " + thumb.getHref());
        thumb.save();
        in = new ByteArrayInputStream( out.toByteArray() );
        thumb.setContent( in );

    }

    public int generateThumbs() {
        return generateThumbs( false );
    }

    public int generateThumbs( boolean skipIfExists ) {
        if( this.isTrash() ) {
            log.debug( "in trash, not generating: : " + this.getPath() );
            return 0;
        }
        List<Thumb> thumbs = Thumb.getThumbSpecs( getParent() );
        int count = 0;
        if( thumbs != null ) {
            for( Thumb t : thumbs ) {
                Folder thumbsFolder = getParent().thumbs( t.suffix, true );
                createThumb( thumbsFolder, t.width, t.height, skipIfExists );
                count++;
            }
        }
        return count;
    }

    public ImageData getImageData() {
        Dimensions dim;
        if( imageData == null ) {
            ImageService svc = new ImageService(); // todo: move to context
            imageData = new ImageData();
            InputStream in = null;
            try {
                in = this.getInputStream();
                dim = svc.getDimenions( in, getName() );
                imageData.setHeight( (int) dim.getY());
                imageData.setWidth( (int) dim.getX());
            } finally {
                StreamUtils.close( in );
            }
//            ImageUtilities.get
        }
        return imageData;
    }

    public int getHeight() {
        return getImageData().getHeight();
    }

    public int getWidth() {
        return getImageData().getWidth();
    }

    @Override
    public Long getMaxAgeSeconds( Auth auth ) {
        return 60 * 60 * 24 * 7 * 4l; // 4 weeks
    }




    public class ImageData {
        private int height;
        private int width;

        /**
         * @return the height
         */
        public int getHeight() {
            return height;
        }

        /**
         * @param height the height to set
         */
        public void setHeight( int height ) {
            this.height = height;
        }

        /**
         * @return the width
         */
        public int getWidth() {
            return width;
        }

        /**
         * @param width the width to set
         */
        public void setWidth( int width ) {
            this.width = width;
        }        
    }
}
