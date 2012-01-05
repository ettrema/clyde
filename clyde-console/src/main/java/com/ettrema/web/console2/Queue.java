package com.ettrema.web.console2;

import com.amazon.thirdparty.Base64;
import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.common.Service;
import com.ettrema.console.Result;
import com.ettrema.grid.Processable;
import com.ettrema.grid.QueueManager;
import com.ettrema.grid.QueueManager.ProcessableMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

/**
 *
 * @author brad
 */
public class Queue extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Queue.class );
    private final QueueManager queueManager;
    private final Service queueProcesor;
    private final Service asyncProcessor;

    Queue( List<String> args, String host, String currentDir, ResourceFactory resourceFactory, QueueManager queueManager, Service queueProcesor, Service asyncProcessor ) {
        super( args, host, currentDir, resourceFactory );
        this.queueManager = queueManager;
        this.queueProcesor = queueProcesor;
        this.asyncProcessor = asyncProcessor;
    }

    @Override
    public Result execute() {
        if( this.args == null || this.args.size() == 0 ) {
            int queueSize = queueManager.getQueueSize();
            log.debug( "queue size: " + queueSize );
            return result( "queue size: " + queueSize );
        } else {
            String opt = args.get( 0 );
            if( opt.equals( "-purge" ) ) {
                int num = 10;
                if( args.size() >= 2 ) {
                    String sNum = args.get( 1 );
                    num = Integer.parseInt( sNum );
                }
                Class type = null;
                if( args.size() >= 3 ) {
                    String sType = args.get( 2 );
                    try {
                        type = Class.forName( sType );
                    } catch( ClassNotFoundException ex ) {
                        return result("class not found: " + sType);
                    }
                }
                return purge( num, type );
            } else if( opt.equals( "-query" ) ) {
                int num = 10;
                if( args.size() >= 2 ) {
                    String sNum = args.get( 1 );
                    num = Integer.parseInt( sNum );
                }
                return query( num );
            } else if( opt.equals( "-pauseQ" ) ) {
                return pauseQueueProcessor();
            } else if( opt.equals( "-resumeQ" ) ) {
                return resumeQueueProcessor();
            } else if( opt.equals( "-pauseAsync" ) ) {
                return pauseAsyncProcessor();
            } else if( opt.equals( "-resumeAsync" ) ) {
                return resumeAsyncProcessor();
            } else {
                return result( "Unknown option: " + opt );
            }
        }
    }

    private Result purge( int num, Class type ) {
        log.warn( "purge: " + num + " - " + type );
        int cnt = 0;
        while( num > 0 ) {
            int toDelete = num;
            if( toDelete > 10 ) toDelete = 10;
            log.warn( " - to delete: " + num );
            List<ProcessableMessage> list = queueManager.receive( toDelete );
            if( list.size() == 0 ) {
                log.warn("no more messages");
                break;
            }
            num = num - toDelete;
            for( ProcessableMessage pm : list ) {
                Processable p = fromMessage( pm );
                if( type == null || type.equals( p.getClass() ) ) {
                    cnt++;
                    queueManager.deleteMessage( pm.id );
                } else {
                    log.warn("not deleting message of class: " + p.getClass());
                }
            }
        }
        return result( "deleted messages: " + cnt );
    }

    private Result query( int num ) {
        String s = "query num: " + num + "<br/>";
        s += "<ul>";
        while( num > 0 ) {
            log.warn( " - query batch size: " + 10 );
            List<ProcessableMessage> list = queueManager.receive( 10 );
            if( list.size() == 0 ) {
                log.warn("no more messages");
                break;
            }
            for( ProcessableMessage pm : list ) {
                Processable p = fromMessage( pm );
                s += "<li>" + p.getClass().getCanonicalName() + " - " + p.toString() + "</p>";
            }
            num = num - 10;
        }
        s += "</ul>";
        return result( s );



    }

    private Processable fromMessage( ProcessableMessage pm ) {
        byte[] bytes = Base64.decode( pm.body );
        ByteArrayInputStream bin = new ByteArrayInputStream( bytes );
        try {
            ObjectInputStream ois = new ObjectInputStream( bin );
            Object oProc = ois.readObject();
            if( oProc instanceof Processable ) {
                return (Processable) oProc;
            } else {
                throw new RuntimeException( "Message is not a Processable. Is a: " + oProc.getClass() );
            }
        } catch( ClassNotFoundException ex ) {
            throw new RuntimeException( ex );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
    }

    private Result pauseQueueProcessor() {
        queueProcesor.stop();
        return result( "paused" );
    }

    private Result resumeQueueProcessor() {
        queueProcesor.start();
        return result( "started" );
    }

    private Result pauseAsyncProcessor() {
        asyncProcessor.stop();
        return result( "stopped async processor" );
    }

    private Result resumeAsyncProcessor() {
        asyncProcessor.start();
        return result( "started async processor" );
    }
}
