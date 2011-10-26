package com.ettrema.web;

import com.bradmcevoy.property.BeanPropertyResource;
import com.ettrema.video.FlashService;


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
    public String getDefaultContentType() {
        return "application/x-flash-video";
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
    protected String getThumbName() {
        return _( FlashService.class ).getThumbName( this );
    }

    public String getStreamingVideoHref() {
        return getHref();
    }

    public String getStreamingVideoUrl() {
        return getUrl();
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
        if( r == null ) {
            return null;
        } else {
            return r.getUrl();
        }
    }
}
