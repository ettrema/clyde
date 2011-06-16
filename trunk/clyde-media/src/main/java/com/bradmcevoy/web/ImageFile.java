package com.bradmcevoy.web;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.media.ThumbProcessor;
import com.bradmcevoy.property.BeanPropertyResource;
import com.bradmcevoy.web.image.Dimensions;
import com.bradmcevoy.web.image.ImageService;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.ettrema.context.RequestContext._;

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



    public int generateThumbs() {
        return generateThumbs( false );
    }

    public int generateThumbs( boolean skipIfExists ) {        
        return generateThumbs( skipIfExists, null );
    }

    public int generateThumbs( boolean skipIfExists, List<Thumb> thumbs ) {
        if( thumbs == null ) {
            thumbs = Thumb.getThumbSpecs( getParent() );
        }
        try {
            return _(ThumbProcessor.class).generateThumbs( this,thumbs, skipIfExists );
        } catch( FileNotFoundException ex ) {
            throw new RuntimeException( this.getHref(), ex );
        } catch( IOException ex ) {
            throw new RuntimeException( this.getHref(),ex );
        }
    }



    public ImageData getImageData() {
        return imageData(true);
    }

    public void setImageData(ImageData data) {
        this.imageData = data;
    }

    public ImageData imageData(boolean create) {
        Dimensions dim;
        if( imageData == null && create ) {
            imageData = new ImageData();
            InputStream in = null;
            try {
                in = this.getInputStream();
                dim = _(ImageService.class).getDimenions( in, getName() );
                imageData.setHeight( (int) dim.getY());
                imageData.setWidth( (int) dim.getX());
            } finally {
                StreamUtils.close( in );
            }
//            ImageUtilities.get
        }
        return imageData;
    }

    public Integer getHeight() {
        ImageData imgData = getImageData();
        if( imgData == null ) return null;
        return imgData.getHeight();
    }

    public Integer getWidth() {
        ImageData imgData = getImageData();
        if( imgData == null ) return null;
        return imgData.getWidth();

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
