package com.bradmcevoy.web.manage.synch;

import com.ettrema.context.Context;
import com.ettrema.context.Executable2;
import com.ettrema.context.RootContext;
import java.io.File;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

/**
 *
 * @author brad
 */
public class FileWatcher implements JNotifyListener {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( FileWatcher.class );
    private RootContext rootContext;
    private final File root;
    private final FileLoader fileLoader;

    public FileWatcher( RootContext rootContext, File root, FileLoader fileLoader ) {
        this.rootContext = rootContext;
        this.root = root;
        this.fileLoader = fileLoader;
    }

    public void start() {
        String path = root.getAbsolutePath();

        // watch mask, specify events you care about,
        // or JNotify.FILE_ANY for all events.
        int mask = JNotify.FILE_CREATED
            | JNotify.FILE_DELETED
            | JNotify.FILE_MODIFIED
            | JNotify.FILE_RENAMED;

        // watch subtree?
        boolean watchSubtree = true;
        try {
            // add actual watch
            JNotify.addWatch( path, mask, watchSubtree, this );
            log.info( "Now watching files in: " + path);
        } catch( JNotifyException ex ) {
            log.error( "error watching: " + root.getAbsolutePath(), ex );
        }

    }

    public void fileCreated( int wd, String rootPath, String name ) {
        String path = rootPath + File.separator + name;
        final File f = new File( path );
        if( isIgnored( f ) ) {
            return;
        }

        rootContext.execute( new Executable2() {

            public void execute( Context context ) {
                fileLoader.onNewFile( f );
            }
        } );

    }

    public void fileDeleted( int wd, String rootPath, String name ) {
        String path = rootPath + File.separator + name;
        final File f = new File( path );
        if( isIgnored( f ) ) {
            return;
        }

        rootContext.execute( new Executable2() {

            public void execute( Context context ) {
                fileLoader.onDeleted( f );
            }
        } );
    }

    public void fileModified( int wd, String rootPath, String name ) {
        log.trace( "fileModified: " + rootPath + " - " + name);
        String path = rootPath + File.separator + name;
        final File f = new File( path );
        if( isIgnored( f ) ) {
            return;
        }

        rootContext.execute( new Executable2() {

            public void execute( Context context ) {
                fileLoader.onModified( f );
            }
        } );

    }

    public void fileRenamed( int i, String string, String string1, String string2 ) {
    }

    public void initialScan() {
        scan( this.root );
    }

    private void scan( File root ) {
        for( File f : root.listFiles() ) {
            if( !isIgnored( f ) ) {
                if( f.isDirectory() ) {
                    scan( f );
                } else {
                    processFile( f );
                }
            }
        }
    }

    private void processFile( final File f ) {
        rootContext.execute( new Executable2() {

            public void execute( Context context ) {
                if( !fileLoader.exists( f ) ) {
                    fileLoader.onNewFile( f );
                }

            }
        } );
    }

    private boolean isIgnored( File f ) {
        return isAnyParentHidden( f );
    }

    private boolean isAnyParentHidden( File f ) {
        if( f.getName().startsWith( "." ) ) {
            return true;
        } else {
            if( !f.getAbsolutePath().contains( root.getAbsolutePath() ) ) { // reached root
                return false;
            } else {
                return isAnyParentHidden( f.getParentFile() );
            }
        }
    }
}
