package com.bradmcevoy.web;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.property.BeanPropertyResource;
import com.bradmcevoy.video.FlashService;
import com.bradmcevoy.web.component.InitUtils;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;

@BeanPropertyResource( "clyde" )
public class VideoFile extends BinaryFile {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( VideoFile.class );
    private static final long serialVersionUID = 1L;
    private Integer convertedHeight = 450;
    private Integer convertedWidth = 600;

    public VideoFile( String contentType, Folder parentFolder, String newName ) {
        super( contentType, parentFolder, newName );
    }

    public VideoFile( Folder parentFolder, String newName ) {
        super( "video", parentFolder, newName );
    }

    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new VideoFile( parent, newName );
    }

    @Override
    public void populateXml( Element e2 ) {
        super.populateXml( e2 );
        InitUtils.set( e2, "convertedHeight", convertedHeight );
        InitUtils.set( e2, "convertedWidth", convertedWidth );
    }

    @Override
    public void loadFromXml( Element el ) {
        super.loadFromXml( el );
        this.convertedHeight = InitUtils.getInteger( el, "convertedHeight" );
        this.convertedWidth = InitUtils.getInteger( el, "convertedWidth" );
    }

    @Override
    protected void afterSetContent() {
        super.afterSetContent();
    }

    public Integer getConvertedHeight() {
        return convertedHeight;
    }

    public Integer getConvertedWidth() {
        return convertedWidth;
    }

    public void setConvertedHeight( Integer convertedHeight ) {
        this.convertedHeight = convertedHeight;
    }

    public void setConvertedWidth( Integer convertedWidth ) {
        this.convertedWidth = convertedWidth;
    }

    public FlashFile getStreamingVideo() {
        FlashService gen = _( FlashService.class );
        String thumbSpec = gen.getFlashThumbSuffix();
        Folder folderThumbs = this.getParent().thumbs( thumbSpec );
        if( folderThumbs == null ) {
            return null;
        }
        BaseResource resThumb = folderThumbs.childRes( gen.getFlashFileNameForVideo( this ) );
        if( resThumb == null ) return null;

        if( resThumb instanceof FlashFile ) {
            FlashFile thumb = (FlashFile) resThumb;
            return thumb;
        } else {
            return null;
        }
    }

    public String getStreamingVideoHref() {
        FlashFile ff = getStreamingVideo();
        if( ff == null ) return "";
        return ff.getHref();
    }

    public String getStreamingVideoUrl() {
        FlashFile ff = getStreamingVideo();
        if( ff == null ) return "";
        return ff.getUrl();
    }

    @Override
    public HtmlImage thumb( String suffix ) {
        Folder f = this.getParent().thumbs( suffix );
        if( f == null ) {
            log.warn( "no thumb spec: " + suffix + " in " + this.getParent().getUrl() );
            return new NoImageResource();
        }
        BaseResource res = f.childRes( _(FlashService.class).getThumbName( this ) ); 
        if( res instanceof BinaryFile ) {
            return (BinaryFile) res;
        } else {
            return null;
        }
    }

    @Override
    public HtmlImage getThumb() {
        FlashFile ff = getStreamingVideo();
        if( ff == null ) {
            return null;
        } else {
            return ff.getThumb();
        }
    }

    @Override
    public Long getMaxAgeSeconds( Auth auth ) {
        return 60 * 60 * 24 * 7 * 4l; // 4 weeks
    }
}
