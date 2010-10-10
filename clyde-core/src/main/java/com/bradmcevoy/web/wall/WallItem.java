package com.bradmcevoy.web.wall;

import java.util.Date;

/**
 *
 * @author brad
 */
public interface WallItem {

    Date getLastUpdated();

    void pleaseImplementSerializable();
}
