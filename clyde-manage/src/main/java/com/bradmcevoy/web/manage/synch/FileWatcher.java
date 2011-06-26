package com.bradmcevoy.web.manage.synch;

import com.ettrema.common.Service;
import com.ettrema.context.Context;
import com.ettrema.context.Executable2;
import com.ettrema.context.RootContext;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

/**
 *
 * @author brad
 */
public class FileWatcher implements JNotifyListener, Service {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FileWatcher.class);

	{
		String orig = System.getProperty("java.library.path");
		System.out.println("original library path: " + orig);
		String userDir = System.getProperty("user.dir");
		System.out.println("user dir: " + userDir); 
		String newLibPath = orig + ":" + userDir;
		System.out.println("new library path: " + newLibPath);
		System.setProperty("java.library.path", newLibPath);

	}
	private RootContext rootContext;
	private final File root;
	private final FileLoader fileLoader;
	private int watchId = 0;
	private boolean watchFiles = true;
	private boolean initialScan = false;
	Thread thInitialScan;

	public FileWatcher(RootContext rootContext, File root, FileLoader fileLoader) {
		this.rootContext = rootContext;
		this.root = root;
		this.fileLoader = fileLoader;
	}

	public void start() {
		if (watchFiles) {
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
				watchId = JNotify.addWatch(path, mask, watchSubtree, this);
				log.info("Now watching files in: " + path);
			} catch (Throwable ex) {
				log.error("Library path (sys property) is: " + System.getProperty("java.library.path"));
				log.error("error watching: " + root.getAbsolutePath(), ex);
			}
		}

		if (initialScan) {
			thInitialScan = Executors.defaultThreadFactory().newThread(new Runnable() {

				public void run() {
					initialScan();
				}
			});
			thInitialScan.start();
		}
	}

	public void stop() {
		if (watchId > 0) {
			try {
				JNotify.removeWatch(watchId);
			} catch (JNotifyException ex) {
				log.error("Exception stopping jnotify", ex);
			} finally {
				watchId = 0;
			}
			thInitialScan.interrupt();
		}
	}

	public void fileCreated(int wd, String rootPath, String name) {
		String path = rootPath + File.separator + name;
		final File f = new File(path);
		if (isIgnored(f)) {
			return;
		}

		rootContext.execute(new Executable2() {

			public void execute(Context context) {
				fileLoader.onNewFile(f, root);
			}
		});

	}

	public void fileDeleted(int wd, String rootPath, String name) {
		String path = rootPath + File.separator + name;
		final File f = new File(path);
		if (isIgnored(f)) {
			return;
		}

		rootContext.execute(new Executable2() {

			public void execute(Context context) {
				fileLoader.onDeleted(f, root);
			}
		});
	}

	public void fileModified(int wd, String rootPath, String name) {
		log.trace("fileModified: " + rootPath + " - " + name);
		String path = rootPath + File.separator + name;
		final File f = new File(path);
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
