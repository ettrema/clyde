package com.ettrema.web.search;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 */
public class DefaultDirectoryFactory implements DirectoryFactory{

    private final File basedir;

    public DefaultDirectoryFactory(File basedir) {
        this.basedir = basedir;
    }

    public Directory open(String name) throws IOException {
        Directory index = FSDirectory.open(getDir(name));
        return index;
    }

    public File getDir(String name) {
        if( basedir.exists() ) basedir.mkdirs();
        return new File(basedir,name);
    }

    public boolean exists(String name) {
        return getDir(name).exists();
    }
}
