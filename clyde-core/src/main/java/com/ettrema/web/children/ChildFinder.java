package com.ettrema.web.children;

import com.bradmcevoy.http.Resource;
import com.ettrema.web.Folder;
import com.ettrema.web.Templatable;
import java.util.List;

/**
 *
 * @author brad
 */
public interface ChildFinder {
    Resource find(String name, Folder folder);
    
    List<Templatable> getSubPages(Folder folder);
}
