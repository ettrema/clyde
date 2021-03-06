package com.ettrema.web;

import com.bradmcevoy.http.Request;
import com.ettrema.binary.BinaryContainer;
import com.ettrema.binary.ClydeBinaryService;
import com.ettrema.binary.VersionDescriptor;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.property.BeanPropertyResource;
import com.bradmcevoy.utils.FileUtils;
import com.ettrema.utils.CurrentRequestService;
import com.ettrema.web.SimpleEditPage.SimpleEditable;
import com.ettrema.vfs.OutputStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;

@BeanPropertyResource( "clyde" )
public class BinaryFile extends File implements XmlPersistableResource, HtmlImage, Replaceable, BinaryContainer, SimpleEditable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( BinaryFile.class );
    private static final long serialVersionUID = 1L;
    private int contentLength;
    private long crc;
    private boolean firstVersionDone;

    public BinaryFile( String contentType, Folder parentFolder, String newName ) {
        super( contentType, parentFolder, newName );
    }

    public BinaryFile( Folder parentFolder, String newName ) {
        super( "application", parentFolder, newName );
    }

    @Override
    public String getDefaultContentType() {
        // since binary files can represent many different content types
        // we try to infer from the file name
        return ContentTypeUtil.getContentTypeString( getName() );
    }

    public boolean isFirstVersionDone() {
        return firstVersionDone;
    }

    public void setFirstVersionDone( boolean firstDone ) {
        this.firstVersionDone = firstDone;
    }

    @Override
    protected BaseResource copyInstance( Folder parent, String newName ) {
        BinaryFile f = (BinaryFile) super.copyInstance( parent, newName );
        f.save();
        InputStream in = this.getInputStream();
        try {
            f.setContent( in );
        } finally {
            FileUtils.close( in );
        }

        return f;
    }

    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new BinaryFile( parent, newName );
    }

    @Override
    public boolean is( String type ) {
        if( type == null ) {
            return false;
        }
        if( type.equals( "binary" ) ) {
            return true;
        }
        String contentType = getContentType( null );
        if( contentType != null && contentType.contains( type ) ) {
            return true;
        }
        return super.is( type );
    }

    @Override
    public void populateXml( Element e2 ) {
        super.populateXml( e2 );
        e2.setAttribute( "contentLength", contentLength + "" );
        e2.setAttribute( "crc", crc + "" );
        e2.setAttribute( "firstVersionDone", firstVersionDone + "" );

    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException {
        try {
            if( log.isDebugEnabled() ) {
                log.debug( "sendContent: " + getHref() );
            }
            String versionNum = null;
            if( params != null ) {
                versionNum = params.get( "_version" );
            }
            InputStream in = getInputStream( versionNum );
            if( in == null ) {
                log.warn( "Failed to get an inputstream for: " + getHref() );
                return;
            }

            long bytes = StreamUtils.readTo( in, out, true, false );

            out.flush();
            if( log.isDebugEnabled() ) {
                if( bytes > 0 ) {
                    log.debug( "sent bytes: " + bytes );
                }
            }
            if( bytes == 0 ) {
                log.warn( "zero length binary file: " + getNameNodeId() + " - " + getHref() );
            }
        } catch( ReadingException readingException ) {
            log.error( "exception reading data: " + getHref(), readingException );
        } catch( WritingException writingException ) {
            log.debug( "exception writing data: " + getHref(), writingException );
        } catch( Throwable e ) {
            log.error( "Exception sending content", e );
        }
    }

    /**
     * Read the inputstream to find the persisted content size. This is for debugging
     * only.
     *
     * @return
     */
    public int getActualPersistedContentSize() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            sendContent( bout, null, null, null );
            return bout.size();
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
    }

    /**
     * Opens a stream on the binary content. May return no bytes. You must close
     *  the stream.
     * 
     * @return
     */
    public InputStream getInputStream() {
        try {
            return getInputStream( null );
        } catch( BadRequestException ex ) {
            // should never happen cause there is no version
            throw new RuntimeException( ex );
        }
    }

    public InputStream getInputStream( String versionNum ) throws BadRequestException {
        ClydeBinaryService svc = requestContext().get( ClydeBinaryService.class );
        if( svc == null ) {
            throw new RuntimeException( "Missing from context: " + ClydeBinaryService.class.getCanonicalName() );
        }

        return svc.readInputStream( this, versionNum );
    }

    public void setContent( java.io.File file ) {
        FileInputStream in = null;
        try {
            in = new FileInputStream( file );
            setContent( in );
            save();
        } catch( FileNotFoundException ex ) {
            throw new RuntimeException( ex );
        } finally {
            FileUtils.close( in );
        }
    }

    /**
     * Set the binary content by providing an inputstream to for the underlying
     * persistence mechanism to read.
     *
     * Alternatively, use the useOutputStream method if you dont have access
     * to an inputstream
     *
     * @param in
     */
    @Override
    public void setContent( InputStream in ) {
        ClydeBinaryService svc = requestContext().get( ClydeBinaryService.class );
        if( svc == null ) {
            throw new RuntimeException( "Missing from context: " + ClydeBinaryService.class.getCanonicalName() );
        }
        this.contentLength = svc.setContent( this, in );
        if( log.isTraceEnabled() ) {
            log.trace( "setContent: contentLength: " + this.contentLength );
        }
        save(); // This required to save content length. thought this should be happening elsewhere but didnt seem to be
        afterSetContent();
    }

    @Override
    public void replaceContent( InputStream in, Long length ) {
        log.trace( "replaceContent" );
        setContent( in );
        commit();
    }

    /**
     * Set the binary content by writing to an outputstream. Alternatively,
     * use the setContent method to provide an inpustream to read from.
     *
     * The underlying persistence mechanism will close the output stream
     *
     * @param writer - implement this to use the outputstream
     */
    public void useOutputStream( final OutputStreamWriter<Long> writer ) {
        if( writer == null ) {
            throw new NullPointerException( "writer is null" );
        }
        ClydeBinaryService svc = requestContext().get( ClydeBinaryService.class );
        if( svc == null ) {
            throw new RuntimeException( "Missing from context: " + ClydeBinaryService.class.getCanonicalName() );
        }

        this.contentLength = svc.writeToOutputStream( this, writer );
        this.save(); // this is so content length is persisted
        afterSetContent();
    }

    /**
     * Override to do post processing like thumbnail generation
     */
    protected void afterSetContent() {
    }

    public Folder getThumbsFolder() {
        return getThumbsFolder( false );
    }

    public Folder getThumbsFolder( boolean autoCreate ) {
        Folder f = getParent().thumbs( "_sys_thumb", autoCreate );
        return f;
    }

    public HtmlImage getThumb() {
        Folder folderThumbs = getThumbsFolder();
        if( folderThumbs != null ) {
            BaseResource resThumb = folderThumbs.childRes( this.getName() );
            if( resThumb == null ) {
                resThumb = folderThumbs.childRes( this.getName() + ".jpg" );
            }
            if( resThumb != null && resThumb instanceof HtmlImage ) {
                HtmlImage thumb = (HtmlImage) resThumb;
                return thumb;
            }
        }
        return null;
        //return new NoImageResource();
    }

    public String getThumbHref() {
        HtmlImage img = getThumb();
        if( img == null ) {
            return "";
        }
        return img.getHref();
    }

    public HtmlImage getThumbInFolder() {
        String thumbName = FileUtils.preprendExtension( getName(), "thumb" );
        Resource child = this.getParent().child( thumbName );
        if( child instanceof HtmlImage ) {
            return (HtmlImage) child;
        } else {
            return new NoImageResource();
        }
    }

    @Override
    public String getImg() {
        return "<img src='" + getUrl() + "' />";
    }

    @Override
    public String img( String onclick ) {
        return "<img onclick=\"" + onclick + "\" src='" + getUrl() + "' />";
    }

    @Override
    public String getLinkImg() {
        String img = getImg();
        return link( img );
    }

    public String getLinkThumbImg() {
        HtmlImage bf = getThumb();
        return link( bf.getImg() );
    }

    public String linkThumbImg( String onclick ) {
        HtmlImage bf = getThumb();
        if( bf != null ) {
            return "<a href='#' onclick=\"" + onclick + "; return false\" >" + bf.getImg() + "</a>";
        } else {
            return "<a href='#' onclick=\"" + onclick + "; return false\" >" + getName() + "</a>";
        }
    }

    protected String getThumbName() {
        return this.getName();
    }

    public HtmlImage thumb( String suffix ) {
        Folder f = this.getParent().thumbs( suffix );
        if( f == null ) {
            log.warn( "no thumb spec: " + suffix + " in " + this.getParent().getUrl() );
            return null;
        }
        String thumbName = getThumbName();
        BaseResource res = f.childRes( thumbName );
        if( res != null && res instanceof BinaryFile ) {
            return (BinaryFile) res;
        } else {
            log.info( "no thumb with name: " + thumbName + " in folder: " + f.getName() );
            return null;
        }
    }

    @Override
    protected long getDefaultMaxAge( Auth auth ) {
        if( auth == null ) {
            return 60 * 60 * 24 * 7l; // 1 week
        } else {
            return 60 * 60 * 24l; // 1 day
        }
    }

    @Override
    public long getLocalCrc() {
        return crc;
    }

    @Override
    public void setLocalCrc( long value ) {
        this.crc = value;
    }

    public long getCrc() {             
        String versionNum = null;
        Request req = _(CurrentRequestService.class).request();
        if( req != null && req.getParams() != null ) {
            versionNum = req.getParams().get( "_version" );
        }
        return _( ClydeBinaryService.class ).getCrc( this, versionNum );
    }

    @Override
    public Long getContentLength() {
        String versionNum = null;
        Request req = _(CurrentRequestService.class).request();
        if( req != null && req.getParams() != null ) {
            versionNum = req.getParams().get( "_version" );
        }
        return _( ClydeBinaryService.class ).getContentLength( this, versionNum );
    }

    @Override
    public Long getLocalContentLength() {
        int i = contentLength;
        return (long) i;
    }

    public List<VersionDescriptor> getVersions() {
        ClydeBinaryService svc = requestContext().get( ClydeBinaryService.class );
        if( svc == null ) {
            log.debug( "no ClydeBinaryService is configured" );
            return null;
        } else {
            return svc.getVersions( this );
        }
    }

    public int getNumVersions() {
        List<VersionDescriptor> versions = getVersions();
        if( versions == null ) {
            return 0;
        } else {
            return versions.size();
        }
    }

    @Override
    public boolean isIndexable() {
        return true;
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        if( this.isInTemplates() ) {
            return 60 * 60 *24 * 7 * 52l; // =31,449,600
        } else {
            return super.getMaxAgeSeconds(auth);
        }
    }

    @Override
    public void setContent(String content) {
        try {
            ByteArrayInputStream bin = new ByteArrayInputStream(content.getBytes("UTF-8"));
            setContent(bin);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getContent() {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            InputStream in = getInputStream();
            StreamUtils.readTo(in, bout);
            return bout.toString("UTF-8");
        } catch (ReadingException ex) {
            throw new RuntimeException(ex);
        } catch (WritingException ex) {
            throw new RuntimeException(ex);
        } catch(UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public PostableResource getEditPage() {
        return new SimpleEditPage(this);
    }
    
    
}
