package com.bradmcevoy.web.console2;

import com.amazon.s3.AWSAuthConnection;
import com.amazon.s3.Bucket;
import com.amazon.s3.ListAllMyBucketsResponse;
import com.amazon.s3.ListBucketResponse;
import com.amazon.s3.ListEntry;
import com.bradmcevoy.context.RequestContext;
import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.console.Result;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class S3List extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( S3List.class );

    S3List( List<String> args, String host, String currentDir, ResourceFactory resourceFactory ) {
        super( args, host, currentDir, resourceFactory );
    }

    public Result execute() {
        try {
            AWSAuthConnection con = RequestContext.getCurrent().get( AWSAuthConnection.class );
            if( args.size() > 0 ) {
                String bucketName = args.get( 0 );
                log.debug( "list: " + bucketName );
                return listBucket( con, bucketName );
            } else {
                return listAllBuckets( con );
            }
        } catch( MalformedURLException ex ) {
            return result( "error: " + ex );
        } catch( IOException ex ) {
            return result( "error: " + ex );
        }
    }

    private Result listAllBuckets( AWSAuthConnection con ) throws IOException {
        ListAllMyBucketsResponse resp = con.listAllMyBuckets( new HashMap() );
        if( resp.entries != null && resp.entries.size() > 0 ) {
            StringBuffer sb = new StringBuffer( "<p>Found: " + resp.entries.size() + " buckets</p>" );
            sb.append( "<ul>" );
            for( Object oBucket : resp.entries ) {
                if( oBucket instanceof Bucket ) {
                    Bucket b = (Bucket) oBucket;
                    sb.append( "<li>" + b.name + "</li>" );
                } else {
                    return result( "Not a bucket. Is a: " + oBucket.getClass() );
                }
            }
            sb.append( "</ul>" );
            return result( sb.toString() );
        } else {
            return result( "no buckets" );
        }
    }

    private Result listBucket( AWSAuthConnection con, String bucketName ) {
        Collection<ListEntry> items = findItems(con, bucketName);
        if( items.size() > 0 ) {
            StringBuffer sb = new StringBuffer( "<p>Found: " + items.size() + " items</p>" );
            sb.append( "<table>" );
            sb.append("<tr><th>Name</th><th>Size</th></tr>");
            for( ListEntry e : items ) {
                sb.append("<tr><td>" + e.key + "</td><td>" + e.size + "</td></tr>");
            }
            sb.append( "</table>" );
            return result( sb.toString() );
        } else {
            return result( "no items in bucket" );
        }
    }

    public static Collection<ListEntry> findItems( AWSAuthConnection con, String bucketName ) {
        Map<String,ListEntry> items = new HashMap<String,ListEntry>();
        findItems( con, bucketName, items, null );
        return items.values();
    }

    public static void findItems( AWSAuthConnection con, String bucketName, Map<String,ListEntry> items, String marker ) {
        log.debug( "find: from marker: " + marker);
        ListBucketResponse resp = con.listBucket( bucketName, null, marker, 1000, null );
        String lastKey = null;
        for( Object o : resp.entries ) {
            if( o instanceof ListEntry) {
                ListEntry e = (ListEntry) o;
                items.put( e.key, e );
                lastKey = e.key;
            } else {
                throw new RuntimeException( "Not a: " + ListEntry.class);
            }
        }

        if( resp.isTruncated ) {
            resp = null;
            findItems( con, bucketName, items, lastKey);
        }
    }
}
