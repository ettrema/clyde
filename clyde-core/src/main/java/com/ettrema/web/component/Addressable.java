package com.ettrema.web.component;

import com.bradmcevoy.common.Path;

/** Represents any object which can be uniquely identified by a path
 */
public interface Addressable {
    Addressable getContainer();
    Path getPath();
    String getName();
}
