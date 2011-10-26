package com.ettrema.binary;

/**
 *
 */
public interface VersionDescriptor {

    String getVersionNum();

    long getCrc();

    String getUserName();

    long getContentLength();

}
