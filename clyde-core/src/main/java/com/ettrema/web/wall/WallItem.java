package com.ettrema.web.wall;

import java.util.Date;

/**
 * A WallItem is largely unstructured data. There are a couple of common methods,
 * but most of the information in the WallItem should be in the form of bean
 * properties which get translated into JSON when the wall is requested by
 * the user agent.
 *
 * @author brad
 */
public interface WallItem {

    Date getLastUpdated();

    void pleaseImplementSerializable();

    String getType();
}
