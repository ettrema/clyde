package com.ettrema.underlay;

import com.ettrema.web.Folder;
import com.ettrema.web.Host;

/**
 *
 * @author brad
 */
public interface UnderlayLocator {
    Host find(UnderlayVector vector);
    Folder getUnderlaysFolder(boolean autocreate);
}
