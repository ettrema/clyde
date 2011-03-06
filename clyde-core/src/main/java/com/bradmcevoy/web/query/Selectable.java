package com.bradmcevoy.web.query;

import com.bradmcevoy.web.Folder;
import java.util.List;

/**
 *
 * @author brad
 */
public interface Selectable {
    List<FieldSource> getRows(Folder from);
}
