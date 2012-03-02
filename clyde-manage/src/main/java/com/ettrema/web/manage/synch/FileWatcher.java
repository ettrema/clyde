package com.ettrema.web.manage.synch;

import com.ettrema.common.Service;
import com.ettrema.context.Context;
import com.ettrema.context.Executable2;
import com.ettrema.context.RootContext;
import com.ettrema.vfs.VfsTransactionManager;
import com.sun.nio.file.ExtendedWatchEventModifier;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.filefilter.DirectoryFileFilter;

/**
 *
 * @author brad
 */
public class FileWatcher implements Service {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FileWatcher.class);
    private static Kind<?>[] events = {StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY};
    private RootContext rootContext;
    private final File root;
    private final FileLoader fileLoader;
    private final FileScanner fileScanner;
    private final ScheduledExecutorService scheduledExecutorService;
    private WatchService watchService;
    private WatchKey watchId;
    private boolean watchFiles = true;
    private boolean initialScan = false;
    private boolean running;    
    private boolean forceReload;
    private ScheduledFuture<?> futureInitial;
    private ScheduledFuture<?> futureScan;

    public FileWatcher(RootContext rootContext, File root, FileLoader fileLoader, ScheduledExecutorService scheduledExecutorService) {
        this.rootContext = rootContext;
        this.root = root;
        this.fileLoader = fileLoader;
        this.scheduledExecutorService = scheduledExecutorService;
        fileScanner = new FileScanner(rootContext, fileLoader);
    }

    @Override
    public void start() {
        running = true;
        final boolean doInitial = initialScan;

        if (initialScan) {
            Runnable rInitialScan = new Runnable() {

                @Override
                public void run() {
                    if (doInitial) {
                        log.info("Starting initial scan for: " + root.getAbsolutePath() + " --------------");

                        rootContext.execute(new Executable2() {

                            @Override
                            public void execute(Context cntxt) {
                                try {
                                    fileScanner.initialScan(forceReload, root);
                                    VfsTransactionManager.commit();
                                    log.info("Finished initial scan for: " + root.getAbsolutePath());
                                } catch (Exception ex) {
                                    VfsTransactionManager.rollback();
                                    log.error("exception loading files", ex);
                                }
                            }
                        });
                    }
                }
            };

            futureInitial = scheduledExecutorService.schedule(rInitialScan, 100, TimeUnit.MILLISECONDS);
        }

        if (watchFiles) {
            Runnable rScan = new Runnable() {

                @Override
                public void run() {
                    doScan();
                }
            };
            log.info("Begin file watch loop: " + root.getAbsolutePath());
            futureScan = scheduledExecutorService.scheduleWithFixedDelay(rScan, 200, 200, TimeUnit.MILLISECONDS);

            try {
                final Path path = FileSystems.getDefault().getPath(root.getAbsolutePath());
                watchService = path.getFileSystem().newWatchService();
                registerWatchDir(FileSystems.getDefault(), root);
                log.info("Now watching files in: " + path);
            } catch (Throwable ex) {
                log.error("error watching: " + root.getAbsolutePath(), ex);
            }
        } else {
            log.info("File watching is off");
        }

    }

    private void registerWatchDir(final FileSystem fs, final File dir) throws IOException {
        final Path path = fs.getPath(dir.getAbsolutePath());
        if (watchService.getClass().getName().contains("Windows")) {
            Modifier m = ExtendedWatchEventModifier.FILE_TREE;
            path.register(watchService, events, m);
        } else {
            path.register(watchService, events);
            for (File child : dir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY)) {
                registerWatchDir(fs, child);
            }
        }
    }

    private void doScan()  {
        WatchKey watchKey;
        watchKey = watchService.poll(); // this call is blocking until events are present
        if( watchKey == null ) {
            return ;
        }
        Watchable w = watchKey.watchable();
        Path watchedPath = (Path) w;
        // poll for file system events on the WatchKey
        for (final WatchEvent<?> event : watchKey.pollEvents()) {
            Kind<?> kind = event.kind();
            if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                Path pathCreated = (Path) event.context();
                File f = new File(watchedPath + File.separator + pathCreated);
                fileCreated(f);
            } else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                Path pathDeleted = (Path) event.context();
                File f = new File(watchedPath + File.separator + pathDeleted);
                fileDeleted(f);
            } else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                Path pathModified = (Path) event.context();
                File f = new File(watchedPath + File.separator + pathModified);
                fileModified(f);
            }
        }

        // if the watched directed gets deleted, get out of run method
        if (!watchKey.reset()) {
            log.info("Watch is no longer valid");
            watchKey.cancel();
            stop();
        }

    }

    @Override
    public void stop() {
        running = false;
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException ex) {
            }
        }
        
        if( futureInitial != null ) {
            futureInitial.cancel(true);
            futureInitial = null;
        }
        if(futureScan != null) {
            futureScan.cancel(true);
            futureScan = null;
        }
    }

    public void fileCreated(final File f) {
        if (fileScanner.isIgnored(f, root)) {
            return;
        }
        log.info("fileCreated: " + f.getAbsolutePath());

        rootContext.execute(new Executable2() {

            @Override
            public void execute(Context context) {
                try {
                    fileLoader.onNewFile(f, root);
                    VfsTransactionManager.commit();
                } catch (Exception ex) {
                    VfsTransactionManager.rollback();
                }
            }
        });

    }

    public void fileDeleted(final File f) {
        if (fileScanner.isIgnored(f, root)) {
            return;
        }
        log.info("fileDeleted: " + f.getAbsolutePath());
        rootContext.execute(new Executable2() {

            @Override
            public void execute(Context context) {
                try {
                    fileLoader.onDeleted(f, root);
                    VfsTransactionManager.commit();
                } catch (Exception ex) {
                    VfsTransactionManager.rollback();
                }
            }
        });
    }

    public void fileModified(final File f) {
        log.info("fileModified: " + f.getAbsolutePath());
        if (fileScanner.isIgnored(f, root)) {
            return;
        }

        rootContext.execute(new Executable2() {

            @Override
            public void execute(Context context) {
                try {
                    fileLoader.onModified(f, root);
                    VfsTransactionManager.commit();
                } catch (Exception ex) {
                    VfsTransactionManager.rollback();
                }
            }
        });

    }

    public void fileRenamed(int i, String string, String string1, String string2) {
    }

    public void forceReload() throws Exception {
        fileScanner.initialScan(true, root);
    }

    public boolean isWatchFiles() {
        return watchFiles;
    }

    public void setWatchFiles(boolean watchFiles) {
        this.watchFiles = watchFiles;
    }

    public boolean isInitialScan() {
        return initialScan;
    }

    public void setInitialScan(boolean initialScan) {
        this.initialScan = initialScan;
    }

    public boolean isForceReload() {
        return forceReload;
    }

    public void setForceReload(boolean forceReload) {
        this.forceReload = forceReload;
    }        
}
