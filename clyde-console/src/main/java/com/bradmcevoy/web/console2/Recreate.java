package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.io.StreamToStream;
import com.bradmcevoy.web.BinaryFile;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.TextFile;
import com.ettrema.console.Result;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author brad
 */
public class Recreate extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Recreate.class );

    Recreate( List<String> args, String host, String currentDir, ResourceFactory resourceFactory ) {
        super( args, host, currentDir, resourceFactory );
    }

    @Override
    public Result execute() {
        if( args.size() == 0 ) {
            return result( "missing expression" );
        }
        String exp = args.get( 0 );
        Resource r = currentResource();
        StringBuffer sb = new StringBuffer();
        if( r instanceof Folder ) {
            Folder col = (Folder) r;
            for( Resource child : col.getChildren() ) {
                if( hasExtension( child, exp ) ) {
                    sb.append( child.getName()).append( ", ");
                    File output = null;
                    try {
                        if( child instanceof BinaryFile ) {
                            BinaryFile bf = (BinaryFile) child;
                            output = writeToFile( bf );
                        } else if( child instanceof TextFile ) {
                            TextFile tf = (TextFile) child;
                            output = writeToFile( tf );
                        }
                        reCreate( col, output, child.getName() );
                    } finally {
                        if( output != null ) output.delete();
                    }
                }
            }
        } else {
            sb.append( "The current resource is not a collection!" );
        }
        return result( sb.toString() );
    }

    private boolean hasExtension( Resource child, String exp ) {
        return child.getName().endsWith( "." + exp );
    }

    private File writeToFile( BinaryFile bf ) {
        FileOutputStream out = null;
        InputStream contentIn = null;
        try {
            final File temp = File.createTempFile( "recreate-", bf.getName() );
            out = new FileOutputStream( temp );
            final BufferedOutputStream bufout = new BufferedOutputStream( out );
            contentIn = bf.getInputStream();
            StreamToStream.readTo( contentIn, out );
            return temp;
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        } finally {
            closeSafely( out );
            closeSafely( contentIn );
        }
    }

    private void closeSafely( OutputStream out ) {
        if( out != null ) {
            try {
                out.close();
            } catch( IOException ex ) {
                log.warn( "exception closing out stream" );
            }
        }
    }

    private void closeSafely( InputStream in ) {
        if( in != null ) {
            try {
                in.close();
            } catch( IOException ex ) {
                log.warn( "exception closing in stream" );
            }
        }
    }

    private File writeToFile( TextFile tf ) {
        FileOutputStream out = null;
        InputStream contentIn = null;
        try {
            final File temp = File.createTempFile( "recreate-", tf.getName() );
            out = new FileOutputStream( temp );
            final BufferedOutputStream bufout = new BufferedOutputStream( out );
            contentIn = new ByteArrayInputStream( tf.getContent().getBytes() );
            StreamToStream.readTo( contentIn, out );
            return temp;
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        } finally {
            closeSafely( out );
            closeSafely( contentIn );
        }

    }

    private void reCreate( Folder col, File input, String name ) {
        InputStream fin = null;
        BufferedInputStream bufIn = null;
        try {
            fin = new FileInputStream( input );
            bufIn = new BufferedInputStream( fin );
            col.createNew_notx( name, bufIn, input.length(), null );
        } catch( ConflictException ex ) {
            throw new RuntimeException( ex );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        } finally {
            closeSafely( bufIn);
            closeSafely( fin );
        }
    }
}
