package com.bradmcevoy.web.console2;

import com.amazon.s3.AWSAuthConnection;
import com.amazon.s3.AWSAuthConnection.CopyFailedException;
import com.amazon.s3.ListEntry;
import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.console.Result;
import static com.ettrema.context.RequestContext.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class S3Copy extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( S3Copy.class );

    S3Copy( List<String> args, String host, String currentDir, ResourceFactory resourceFactory ) {
        super( args, host, currentDir, resourceFactory );
    }

    public Result execute() {
        AWSAuthConnection con = _( AWSAuthConnection.class );
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
        Collection<ListEntry> items = S3List.findItems( con, source );
        Collection<ListEntry> existingDestItems = S3List.findItems( con, dest );
        Set<String> setExisting = new HashSet<String>();
        for( ListEntry e : existingDestItems ) {
            setExisting.add( e.key );
        }
        List<String> failedItems = new ArrayList<String>();
        if( items.size() > 0 ) {
            int num = 0;
            int numExisting = 0;
            for( ListEntry e : items ) {
                try {                    
                    num++;
                    if( !setExisting.contains( e.key) ) {
                        log.debug( "copying: " + e.key );
                        con.copyItem( source, e.key, dest, e.key );
                    } else {
                        log.debug( "already exists, not copying: " + e.key );
                        numExisting++;
                    }
                } catch( CopyFailedException ex ) {
                    log.error( "Failed to copy item: " + e.key + " Http Status: " + ex.getHttpStatus() );
                    failedItems.add( e.key );
                }
            }
            StringBuffer sbFailed = new StringBuffer();
            if( failedItems.size() > 0 ) {
                sbFailed.append( "<br/>Failed Items<br/>" );
                for( String key : failedItems ) {
                    sbFailed.append( key ).append( ',' );
                }
            }
            return result( "Copied " + num + " items from " + source + " to " + dest + ", not copied existing items: " + numExisting + sbFailed );
        } else {
            return result( "no items in bucket" );
        }
    }
}
