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
import java.util.HashMap;
import java.util.List;

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
        ListBucketResponse resp = con.listBucket( bucketName, null, null, 100000, null );
        if( resp.entries != null && resp.entries.size() > 0 ) {
            StringBuffer sb = new StringBuffer( "<p>Found: " + resp.entries.size() + " items</p>" );
            sb.append( "<table>" );
            sb.append("<tr><th>Name</th><th>Size</th></tr>");
            for( Object o : resp.entries ) {
                if( o instanceof ListEntry) {
                    ListEntry e = (ListEntry) o;
                    sb.append("<tr><td>" + e.key + "</td><td>" + e.size + "</td></tr>");
                } else {
                    return result("Not a ListEntry. Is a: " + o.getClass());
                }
            }
            sb.append( "</table>" );
            return result( sb.toString() );
        } else {
            return result( "no buckets in bucket" );
        }
    }
}
