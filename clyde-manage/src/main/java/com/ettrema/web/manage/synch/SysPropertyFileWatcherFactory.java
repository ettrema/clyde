package com.ettrema.web.manage.synch;

import com.ettrema.common.Service;
import com.ettrema.context.RootContext;
import com.ettrema.web.code.CodeResourceFactory;
import java.io.File;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.lucene.util.NamedThreadFactory;

/**
 * Creates and initialises other FileWatcher's based on system properties.
 * 
 * Typically you'll set properties in your pom.xml file like this:
 * 
 *                                 <systemProperty>
                                    <name>autoloader.xthis.force</name>
                                    <value>${basedir}/src/main/webapp/autoload</value>
                                </systemProperty>                                                                  

* 
* The name has the following parts - autoloader.[name].[force?]
* Where
*   autoloader - this must be the literal "autoloader" to identify the property as representing an autoloader
*   [name] - any arbitrary name. Autoloaders are ordered alphabetically
*   [force?] - optional literal "force". If present the autoloader will do an initial scan with forceReload = true. Good for applying overrides
* 
* The value has the following parts - [path],[host]
* Where:
*   [path] - is the path to autoload and watch
*   ,[host] - optional, identifies the host name to load into
 *
 * @author brad
 */
public class SysPropertyFileWatcherFactory implements Service {
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SysPropertyFileWatcherFactory.class);
    
    private final RootContext rootContext;
    private final FileLoader defaultFileLoader;
    private final CodeResourceFactory codeResourceFactory;
    private final ErrorReporter errorReporter;
    private final Map<String,FileLoader> mapOfFileLoadersByHost = new HashMap<>();
    private final ScheduledExecutorService scheduledExecutorService;
    
    private String propertyNamePrefix = "autoloader.";
    
    private List<FileWatcher> fileWatchers;

    public SysPropertyFileWatcherFactory(RootContext rootContext, FileLoader fileLoader, CodeResourceFactory codeResourceFactory, ErrorReporter errorReporter) {
        this.rootContext = rootContext;
        this.defaultFileLoader = fileLoader;
        this.codeResourceFactory = codeResourceFactory;
        this.errorReporter = errorReporter;
        scheduledExecutorService = Executors.newScheduledThreadPool(1, new NamedThreadFactory(this.getClass().getCanonicalName()));        
    }
    
    
    @Override
    public void start() {
        fileWatchers = new ArrayList<>();
        Properties props = System.getProperties();
        // load in alphabetical order so its deterministic
        List<String> names = new ArrayList<>(props.stringPropertyNames());
        Collections.sort(names);
        for( String p : names ) {
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
        
        boolean forceReload = propName.endsWith(".force"); // force reload if the name is like autoloader.something.force
        
        log.info("addWatch - raw path: " + path + " - absolute path: " + dirToWatch.getAbsolutePath());
        if( dirToWatch.exists() ) {
            if( dirToWatch.isDirectory())  {
                FileWatcher fw = new FileWatcher(rootContext, dirToWatch, fileLoader, scheduledExecutorService);
                fw.setInitialScan(true);
                fw.setWatchFiles(true);
                fw.setForceReload(forceReload);
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
