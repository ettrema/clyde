package com.ettrema.web.search;

import java.io.IOException;
import org.apache.lucene.store.Directory;

/**
 *
 * @author brad
 */
public interface DirectoryFactory {

    /**
     * Open the directory
     *
     * @param name
     * @return
     * @throws IOException
     */
    Directory open(String name) throws IOException;

    /**
     * Has the index been created?
     * 
     * @return
     */
    boolean exists(String name);

}
