package com.bradmcevoy.web.children;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.Folder;

/**
 *
 * @author brad
 */
public interface ChildFinder {
    Resource find(String name, Folder folder);
}
