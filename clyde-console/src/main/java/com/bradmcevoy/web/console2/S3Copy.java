package com.bradmcevoy.web.console2;

import com.amazon.s3.AWSAuthConnection;
import com.amazon.s3.AWSAuthConnection.CopyFailedException;
import com.amazon.s3.ListBucketResponse;
import com.amazon.s3.ListEntry;
import com.bradmcevoy.context.RequestContext;
import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.console.Result;
import java.util.List;

public class S3Copy extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( S3Copy.class );

    S3Copy( List<String> args, String host, String currentDir, ResourceFactory resourceFactory ) {
        super( args, host, currentDir, resourceFactory );
    }

    public Result execute() {
        AWSAuthConnection con = RequestContext.getCurrent().get( AWSAuthConnection.class );
        if( args.size() > 0 ) {
            String source = args.get( 0 );
            if( args.size() > 1 ) {
                String dest = args.get( 1 );
                log.debug( "copy: " + source + " --> " + dest );
                try {
                    return copy( con, source, dest );
                } catch( Exception ex ) {
                    log.error( "copy failed", ex );
                    return result( "Failed: " + ex );
                }
            } else {
                return result( "Please enter a destination bucket" );
            }
        } else {
            return result( "Please enter a source bucket" );
        }
    }

    private Result copy( AWSAuthConnection con, String source, String dest ) throws Exception {
        ListBucketResponse resp = con.listBucket( source, null, null, 100000, null );
        if( resp.entries != null && resp.entries.size() > 0 ) {
            int num = 0;
            for( Object o : resp.entries ) {
                if( o instanceof ListEntry ) {
                    ListEntry e = (ListEntry) o;
                    try {
                        log.debug( "copying: " + e.key);
                        num++;
                        con.copyItem( source, e.key, dest, e.key );
                    } catch( CopyFailedException ex ) {
                        throw new RuntimeException( "Failed to copy item: " + e.key + " Http Status: " + ex.getHttpStatus() );
                    }
                } else {
                    return result( "Not a ListEntry. Is a: " + o.getClass() );
                }
            }
            return result( "Copied " + num + " items from " + source + " to " + dest );
        } else {
            return result( "no items in bucket" );
        }
    }
}
