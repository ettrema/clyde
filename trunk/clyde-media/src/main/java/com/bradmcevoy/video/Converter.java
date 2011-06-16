package com.bradmcevoy.video;

import java.io.OutputStream;

/**
 *
 */
public interface Converter {

    void close();

    Long convert(OutputStream out, String outputFormat);

    Long convert(OutputStream out, String outputFormat, int height, int width);

    Long generateThumb(int height, int width, OutputStream out, String outputFormat);

}
