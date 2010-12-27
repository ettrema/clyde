package com.bradmcevoy.web;

import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

public class TextFile extends File implements SimpleEditPage.SimpleEditable, Replaceable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( TextFile.class );
    private static final long serialVersionUID = 1L;
    private String content;

    public TextFile( String contentType, Folder parent, String name ) {
        super( contentType, parent, name );
    }

    public TextFile( Folder parent, String name ) {
        super( "text", parent, name );
    }

    @Override
    public String getDefaultContentType() {
        // since binary files can represent many different content types
        // we try to infer from the file name
        return ContentTypeUtil.getContentTypeString( getName() );
    }
    
    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new TextFile( parent, newName );
    }

    @Override
    public boolean is( String type ) {
        if( type == null ) return false;
        if( super.is( type ) ) return true;
        if( type.equals( "text" ) ) return true;
        String ct = getContentType( null );
        if( ct == null ) return false;
        return ct.contains( type );
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public void setContent( String content ) {
        this.content = content;
    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException {
        if( content == null ) {
            log.debug( "no content for: " + this.getPath() );
        } else {
            out.write( content.getBytes() );
        }
    }

    @Override
    public Long getContentLength() {
        if( content == null ) return (long) 0;
        return (long) content.length();
    }

    @Override
    public PostableResource getEditPage() {
        return new SimpleEditPage( this );
    }

    @Override
    protected BaseResource copyInstance( Folder parent, String newName ) {
        BaseResource newRes = super.copyInstance( parent, newName );
        ( (TextFile) newRes ).setContent( content );
        return newRes;
    }

    @Override
    void setContent( InputStream in ) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamUtils.readTo( in, out );
            setContent( out.toString() );
        } catch( ReadingException ex ) {
            throw new RuntimeException( ex );
        } catch( WritingException ex ) {
            throw new RuntimeException( ex );
        }

    }

    @Override
    public void replaceContent( InputStream in, Long length ) {
        log.debug( "replaceContent" );
        setContent( in );
        this.save();
        this.commit();
    }

    @Override
    public boolean isIndexable() {
        return true;
    }

    public long getCrc() {
        try {
            CheckedInputStream cin = new CheckedInputStream( new ByteArrayInputStream( content.getBytes() ), new CRC32() );
            IOUtils.copy( cin, new NullOutputStream() );
            long crc = cin.getChecksum().getValue();
            return crc;
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
    }
}
