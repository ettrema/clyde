package com.ettrema.web.manage.synch;

import com.ettrema.common.Service;
import com.ettrema.context.Context;
import com.ettrema.context.Executable2;
import com.ettrema.context.RootContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 *
 * @author brad
 */
public class FileWatcher implements Service {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FileWatcher.class);

    private RootContext rootContext;
    private final File root;
    private final FileLoader fileLoader;
    private WatchService watchService;
    private WatchKey watchId;
    private boolean watchFiles = true;
    private boolean initialScan = false;
    private boolean running;
    Thread thScan;

    public FileWatcher(RootContext rootContext, File root, FileLoader fileLoader) {
        this.rootContext = rootContext;
        this.root = root;
        this.fileLoader = fileLoader;
    }

    @Override
    public void start() {
        final Path path = FileSystems.getDefault().getPath(root.getAbsolutePath());
        if (watchFiles) {
            try {
                watchService = path.getFileSystem().newWatchService();
                watchId = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
                log.info("Now watching files in: " + path);
            } catch (Throwable ex) {
                log.error("error watching: " + root.getAbsolutePath(), ex);
            }
        } else {
            log.info("File watching is off");
        }

        running = true;
        final boolean doInitial = initialScan;
        thScan = Executors.defaultThreadFactory().newThread(new Runnable() {

            @Override
            public void run() {
                if (doInitial) {
                    log.info("Initial scan is on: " + path);
                    initialScan();
                }
                try {
                    while (running) {
                        log.info("Begin file watch loop: " + path);
                        doScan();
                    }
                } catch (InterruptedException interruptedException) {
                    System.out.println("Watch interrupted");
                }
                log.info("File watcher has exited: " + path);
            }
        });
        thScan.start();

    }

    private void doScan() throws InterruptedException {
        WatchKey watchKey;
        watchKey = watchService.take(); // this call is blocking until events are present

        // poll for file system events on the WatchKey
        for (final WatchEvent<?> event : watchKey.pollEvents()) {
            Kind<?> kind = event.kind();
            if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                Path pathCreated = (Path) event.context();
                fileCreated(pathCreated.toFile());
            } else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                Path pathDeleted = (Path) event.context();
                fileDeleted(pathDeleted.toFile());
            } else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                Path pathModified = (Path) event.context();
                fileModified(pathModified.toFile());
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
        if (thScan != null) {
            thScan.interrupt();
            thScan = null;
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException ex) {
            }
        }
    }

    public void fileCreated(final File f) {
        if (isIgnored(f)) {
            return;
        }

        rootContext.execute(new Executable2() {

            public void execute(Context context) {
                fileLoader.onNewFile(f, root);
            }
        });

    }

    public void fileDeleted(final File f) {
        if (isIgnored(f)) {
            return;
        }

        rootContext.execute(new Executable2() {

            public void execute(Context context) {
                fileLoader.onDeleted(f, root);
            }
        });
    }

    public void fileModified(final File f) {
        log.trace("fileModified: " + f.getAbsolutePath());
        if (isIgnored(f)) {
            return;
        }

        rootContext.execute(new Executable2() {

            public void execute(Context context) {
                fileLoader.onModified(f, root);
            }
        });

    }

    public void fileRenamed(int i, String string, String string1, String string2) {
    }

    public void forceReload() {
        initialScan(true);
    }

    public void initialScan() {
        initialScan(false);
    }

    public void initialScan(boolean forceReload) {
        long t = System.currentTimeMillis();
        log.info("begin full scan");
        startScan(this.root, forceReload);
        log.info("------------------------------------");
        log.info("Completed full scan in " + (System.currentTimeMillis() - t) / 1000 + "secs");
        log.info("------------------------------------");
    }

    private void startScan(File root, boolean forceReload) {
        log.info("scan files in " + root.getAbsolutePath());
        DirectoryListing listing = new DirectoryListing(root);
        // First process the templates folder, if present.
        if (listing.templates != null) {
            startScan(listing.templates, forceReload);
        }
        scanDir(root, forceReload);
    }

    private void scanDir(File dir, boolean forceReload) {
        DirectoryListing listing = new DirectoryListing(dir);

        processFile(dir, true); // force load of dirs for metadata

        for (File f : listing.files) {
            processFile(f, forceReload);
        }

        for (File f : listing.subdirs) {
            startScan(f, forceReload);
        }
    }

    private void processFile(final File f, final boolean forceReload) {
        rootContext.execute(new Executable2() {

            public void execute(Context context) {
                if (forceReload || fileLoader.isNewOrUpdated(f, root)) {
                    fileLoader.onNewFile(f, root);
                }

            }
        });
    }

    private boolean isIgnored(File f) {
        return isAnyParentHidden(f);
    }

    private boolean isAnyParentHidden(File f) {
        if (f.getName().startsWith(".")) {
            return true;
        } else {
            if (!f.getAbsolutePath().contains(root.getAbsolutePath())) { // reached root
                return false;
            } else {
                return isAnyParentHidden(f.getParentFile());
            }
        }
    }

    private class DirectoryListing {

        File templates;
        final List<File> files = new ArrayList<File>();
        final List<File> subdirs = new ArrayList<File>();

        public DirectoryListing(File parent) {
            for (File f : parent.listFiles()) {
                if (!isIgnored(f)) {
                    if (f.isDirectory()) {
                        if ("templates".equals(f.getName())) {
                            this.templates = f;
                        } else {
                            this.subdirs.add(f);
                        }
                    } else {
                        this.files.add(f);
                    }
                }
            }

        }
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
}
