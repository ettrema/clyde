package com.bradmcevoy.media;

import com.bradmcevoy.web.VideoFile;

/**
 *
 * @author brad
 */
public interface StreamingVideoGenerator {
    void generateStreamingVideo(VideoFile source);
}
