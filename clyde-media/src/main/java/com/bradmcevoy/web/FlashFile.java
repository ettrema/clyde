package com.bradmcevoy.web;

import com.bradmcevoy.property.BeanPropertyResource;
import com.bradmcevoy.video.FlashService;

import static com.ettrema.context.RequestContext._;

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
        _(FlashService.class).generateThumb( this );
    }
}
