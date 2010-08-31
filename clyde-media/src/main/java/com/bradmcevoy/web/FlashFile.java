package com.bradmcevoy.web;

import com.bradmcevoy.io.FileUtils;
import com.bradmcevoy.property.BeanPropertyResource;
import com.bradmcevoy.video.FFMPEGConverter;
import com.ettrema.vfs.OutputStreamWriter;
import java.io.InputStream;
import java.io.OutputStream;

@BeanPropertyResource( value = "clyde" )
public class FlashFile extends BinaryFile {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( FlashFile.class );
    private static final long serialVersionUID = 1L;

    public FlashFile( String contentType, Folder parentFolder, String newName ) {
        super( contentType, parentFolder, newName );
    }

    public FlashFile( Folder parentFolder, String newName ) {
        super( "application/x-flash-video", parentFolder, newName );
    }

    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new FlashFile( parent, newName );
    }

    @Override
    public boolean is( String type ) {
        if( type.equals( "flash" ) ) {
            return true;
        } else {
            return super.is( type );
        }
    }

    @Override
    protected void afterSetContent() {
        super.afterSetContent();
        try {
            generateThumb();
        } catch( Exception e ) {
            log.warn( "Failed to generate thumbnail for: " + this.getHref(), e );
        }
    }

    public String getThumbMagicNumber() {
        Folder thumbs = this.getThumbsFolder( false );
        if( thumbs == null ) return "";
        String thumbName = this.getName() + ".jpg";
        BaseResource r = thumbs.childRes( thumbName );
        return r.getMagicNumber();
    }

    @Override
    public String getThumbHref() {
        Folder thumbs = this.getThumbsFolder( false );
        if( thumbs == null ) return "";
        String thumbName = this.getName() + ".jpg";
        BaseResource r = thumbs.childRes( thumbName );
        return r.getUrl();
    }

    public void generateThumb() {
        Folder thumbs = this.getThumbsFolder( true );
        String thumbName = this.getName() + ".jpg";
        BaseResource r = thumbs.childRes( thumbName );
        if( r != null ) r.delete();
        BinaryFile thumb = new BinaryFile( thumbs, thumbName );
        thumb.save();

        InputStream in = null;
        try {
            in = getInputStream();
            if( in == null ) {
                log.warn( "No inputstream for: " + this.getHref() );
            } else {
                final FFMPEGConverter c = new FFMPEGConverter( in, "flv" );
                thumb.useOutputStream( new OutputStreamWriter<Long>() {

                    @Override
                    public Long writeTo( final OutputStream out ) {
                        log.debug( "using outputstream for conversion" );
                        return c.generateThumb( 60, 80, out, "jpeg" );
                    }
                } );
            }
        } finally {
            FileUtils.close( in );
        }
    }
}
