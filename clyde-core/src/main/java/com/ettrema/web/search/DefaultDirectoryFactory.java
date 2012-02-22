package com.ettrema.web.search;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 */
public class DefaultDirectoryFactory implements DirectoryFactory{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultDirectoryFactory.class);
    
    private final File basedir;

    public DefaultDirectoryFactory(File basedir) {
        this.basedir = basedir;
        if( !basedir.exists() ) {
            log.info("Search index base directory does not exist, will attempt to create: " + basedir.getAbsolutePath());
            if( !basedir.mkdirs() ) {
                log.error("Failed to create search index base directory. Search will not be available");
            }
        } else {
            log.info("Using search index base directory: " + basedir.getAbsolutePath());
        }
    }

    @Override
    public Directory open(String name) throws IOException {
        Directory index = FSDirectory.open(getDir(name));
        return index;
    }

    public File getDir(String name) {        
        File indexDir = new File(basedir,name);
        return indexDir;
    }

    @Override
    public boolean exists(String name) {
        return getDir(name).exists();
    }
}
