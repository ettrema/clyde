package com.ettrema.web.wall;

import java.util.Date;

/**
 *
 * @author brad
 */
public interface WallItem {

    Date getLastUpdated();

    void pleaseImplementSerializable();

    String getType();
}
