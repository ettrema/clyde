package com.ettrema.web.manage.synch;

import com.ettrema.common.Service;
import com.ettrema.context.RootContext;
import com.ettrema.web.code.CodeResourceFactory;
import java.io.File;
import java.util.*;

/**
 * Creates and initialises other FileWatcher's based on system properties.
 *
 * @author brad
 */
public class SysPropertyFileWatcherFactory implements Service {
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SysPropertyFileWatcherFactory.class);
    
    private final RootContext rootContext;
    private final FileLoader defaultFileLoader;
    private final CodeResourceFactory codeResourceFactory;
    private final ErrorReporter errorReporter;
    private final Map<String,FileLoader> mapOfFileLoadersByHost = new HashMap<String, FileLoader>();
    
    private String propertyNamePrefix = "autoloader.";
    
    private List<FileWatcher> fileWatchers;

    public SysPropertyFileWatcherFactory(RootContext rootContext, FileLoader fileLoader, CodeResourceFactory codeResourceFactory, ErrorReporter errorReporter) {
        this.rootContext = rootContext;
        this.defaultFileLoader = fileLoader;
        this.codeResourceFactory = codeResourceFactory;
        this.errorReporter = errorReporter;
    }
    
    
    @Override
    public void start() {
        fileWatchers = new ArrayList<>();
        Properties props = System.getProperties();
        for( String p : props.stringPropertyNames() ) {
            if( p.startsWith(propertyNamePrefix)) {
                addWatch(props, p);
            }
        }
    }

    private void addWatch(Properties props, String propName) throws RuntimeException {
        String watchInfo = props.getProperty(propName);
        log.info("addWatch: " + watchInfo);
        String[] watchInfoArr = watchInfo.split(",");
        String path = watchInfoArr[0];
        FileLoader fileLoader = defaultFileLoader;
        if( watchInfoArr.length > 1 ) {
            String loadIntoHost = watchInfoArr[1];
            log.info("addWatch - loading into host: " + loadIntoHost);
            fileLoader = getOrCreateFileLoader(loadIntoHost);
        }
        File dirToWatch = new File(path);
        log.info("addWatch - raw path: " + path + " - absolute path: " + dirToWatch.getAbsolutePath());
        if( dirToWatch.exists() ) {
            if( dirToWatch.isDirectory())  {
                FileWatcher fw = new FileWatcher(rootContext, dirToWatch, fileLoader);
                fw.setInitialScan(true);
                fw.setWatchFiles(true);
                fw.start();
                fileWatchers.add(fw);
            } else {
                throw new RuntimeException("Watch directory isnt a directory: " + dirToWatch.getAbsolutePath() + " in system property: " + propName);
            }
        } else {
            throw new RuntimeException("Watch directory doesnt exist: " + dirToWatch.getAbsolutePath() + " in system property: " + propName);
        }
    }

    @Override
    public void stop() {
        if( fileWatchers != null ) {
            for(FileWatcher fw : fileWatchers) {
                fw.stop();
            }
        }
    }

    private FileLoader getOrCreateFileLoader(String hostName) {
        if( hostName == null || hostName.length() == 0 ) {
            return defaultFileLoader;
        } else {
            FileLoader fl = mapOfFileLoadersByHost.get(hostName);
            if( fl == null ) {
                FileTransport fileTransport = new DirectFileTransport(hostName, codeResourceFactory);
                fl = new FileLoader(errorReporter, fileTransport);
                mapOfFileLoadersByHost.put(hostName, fl);
            }
            return fl;
        }
    }
    
}
