package com.bradmcevoy.web;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.utils.FileUtils;
import com.bradmcevoy.vfs.OutputStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import org.jdom.Element;

public class BinaryFile extends File implements XmlPersistableResource, HtmlImage {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( BinaryFile.class );
    private static final long serialVersionUID = 1L;
    private int contentLength;
    private long crc;

    public BinaryFile( String contentType, Folder parentFolder, String newName ) {
        super( contentType, parentFolder, newName );
    }

    public BinaryFile( Folder parentFolder, String newName ) {
        super( "application", parentFolder, newName );
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
        if( type == null ) return false;
        if( type.equals( "binary" ) ) return true;
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
    }

    @Override
    public String checkRedirect( Request request ) {
        return null;
    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException {
        try {
            if( log.isDebugEnabled() ) {
                log.debug( "sendContent: " + getHref() );
            }
            InputStream in = getInputStream();
            if( in == null ) {
                log.warn( "Failed to get an inputstream for: " + getHref());
                return;
            }
            long bytes = StreamUtils.readTo( in, out, true, false );
            if( log.isDebugEnabled() ) {
                log.debug( "sent bytes: " + bytes );
            }
        } catch( ReadingException readingException ) {
            log.error( "exception reading data: " + getHref(), readingException);
        } catch( WritingException writingException ) {
            log.error( "exception writing data: " + getHref(), writingException);
        } catch(Throwable e) {
            log.error( "Exception sending content", e);
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
        return this.nameNode.getBinaryContent();
    }

    @Override
    public Long getContentLength() {
        int i = contentLength;
        return (long) i;
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

    @Override
    public void setContent( InputStream in ) {
        CheckedInputStream cin = new CheckedInputStream( in, new CRC32() );
        contentLength = (int) this.nameNode.setBinaryContent( cin );
        if( contentLength == 0 ) {
            log.warn("zero size file: " + getHref());
        }
        this.crc = cin.getChecksum().getValue();
        log.debug( "setContent: new contentLength: " + this.getContentLength() + " crc=" + crc );

        save(); // This required to save content length. thought this should be happening elsewhere but didnt seem to be
        afterSetContent();
    }

    public void useOutputStream( final OutputStreamWriter<Long> writer ) {
        if( writer == null ) throw new NullPointerException( "writer is null" );
        long l = this.nameNode.writeToBinaryOutputStream( writer );
        int i = (int) l;
        this.contentLength = i;
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
        return this.getParent().thumbs( "thumb", autoCreate );
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
        return new NoImageResource();
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
        String s = link( bf.img( onclick ) );
        return s;
    }

    public HtmlImage thumb( String suffix ) {
        Folder f = this.getParent().thumbs( suffix );
        if( f == null ) {
            log.warn( "no folder: " + suffix );
            return new NoImageResource();
        }
        BaseResource res = f.childRes( this.getName() );
        if( res != null && res instanceof BinaryFile ) {
            return (BinaryFile) res;
        } else {
            return new NoImageResource();
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

    public long getCrc() {
        return crc;
    }

}
