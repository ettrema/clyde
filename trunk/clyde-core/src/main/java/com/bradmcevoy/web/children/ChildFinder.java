package com.bradmcevoy.web.children;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Templatable;
import java.util.List;

/**
 *
 * @author brad
 */
public interface ChildFinder {
    Resource find(String name, Folder folder);
    
    List<Templatable> getSubPages(Folder folder);
}
