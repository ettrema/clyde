package com.ettrema.web.manage.synch;

import com.ettrema.common.Service;
import com.ettrema.context.Context;
import com.ettrema.context.Executable2;
import com.ettrema.context.RootContext;
import com.ettrema.underlay.UnderlayLocator;
import com.ettrema.vfs.VfsSession;
import com.ettrema.web.Folder;
import com.ettrema.web.Host;
import com.ettrema.web.code.CodeResourceFactory;
import java.io.File;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.lucene.util.NamedThreadFactory;

/**
 * Creates and initialises other FileWatcher's based on system properties.
 *
 *
 * @author brad
 */
public class SysPropertyUnderlayWatcherFactory implements Service {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SysPropertyUnderlayWatcherFactory.class);
    private final UnderlayLocator underlayLocator;
    private final RootContext rootContext;
    private final CodeResourceFactory codeResourceFactory;
    private final ErrorReporter errorReporter;
    private final ScheduledExecutorService scheduledExecutorService;
    private String propertyNamePrefix = "underlay.";
    private List<FileWatcher> fileWatchers;

    public SysPropertyUnderlayWatcherFactory(UnderlayLocator underlayLocator, RootContext rootContext, CodeResourceFactory codeResourceFactory, ErrorReporter errorReporter) {
        this.underlayLocator = underlayLocator;
        this.rootContext = rootContext;
        this.codeResourceFactory = codeResourceFactory;
        this.errorReporter = errorReporter;
        scheduledExecutorService = Executors.newScheduledThreadPool(1, new NamedThreadFactory(this.getClass().getCanonicalName()));
    }

    @Override
    public void start() {
        System.out.println("Starting SysPropertyUnderlayWatcherFactory");
        fileWatchers = new ArrayList<>();
        rootContext.execute(new Executable2() {

            @Override
            public void execute(Context context) {
                Properties props = System.getProperties();
                // load in alphabetical order so its deterministic

                List<String> names = new ArrayList<>(props.stringPropertyNames());
                Collections.sort(names);


                Folder underlaysFolder = underlayLocator.getUnderlaysFolder(true);
                if (underlaysFolder == null) {
                    throw new RuntimeException("Failed to get an underlays folder");
                }
                // underlay property names are like: underlay.5-1.0.1.jplayer-plugin.com.ettrema
                // underlay.5 just indicates an underlay, and the 5 is for ordering                
                for (String p : names) {
                    if (p.startsWith(propertyNamePrefix)) {
                        String path = System.getProperty(p);
                        int firstDelim = p.indexOf("-");
                        p = p.substring(firstDelim+1);
                        addWatch(props, p, path, underlaysFolder);
                    }
                }
                context.get(VfsSession.class).commit();
            }
        });

    }

    /**
     * underlayName is actually a host name
     *
     * @param props
     * @param underlayName
     * @throws RuntimeException
     */
    private void addWatch(Properties props, String underlayName, String path, Folder underlaysFolder) throws RuntimeException {
        log.info("addWatch: " + underlayName);
        FileLoader fileLoader = createFileLoader(underlayName, underlaysFolder);
        File dirToWatch = new File(path);

        log.info("addWatch - raw path: " + path + " - absolute path: " + dirToWatch.getAbsolutePath());
        if (dirToWatch.exists()) {
            if (dirToWatch.isDirectory()) {
                FileWatcher fw = new FileWatcher(rootContext, dirToWatch, fileLoader, scheduledExecutorService);
                fw.setInitialScan(true);
                fw.setWatchFiles(true);
                fw.start();
                fileWatchers.add(fw);
            } else {
                throw new RuntimeException("Watch directory isnt a directory: " + dirToWatch.getAbsolutePath() + " in system property: " + underlayName);
            }
        } else {
            throw new RuntimeException("Watch directory doesnt exist: " + dirToWatch.getAbsolutePath() + " in system property: " + underlayName);
        }
    }

    @Override
    public void stop() {
        if (fileWatchers != null) {
            for (FileWatcher fw : fileWatchers) {
                fw.stop();
            }
        }
    }

    private FileLoader createFileLoader(String hostName, Folder underlaysFolder) {
        // Check that the host exists, create it if not
        System.out.println("createFileLoader: " + hostName);
        Host h = (Host) underlaysFolder.child(hostName);
        if (h == null) {
            h = new Host(underlaysFolder, hostName);
            System.out.println("create new host: " + h.getNameNodeId());            
            h.save();
        }
        FileTransport fileTransport = new DirectFileTransport(h.getName(), codeResourceFactory);
        FileLoader fl = new FileLoader(errorReporter, fileTransport);
        return fl;
    }
}
