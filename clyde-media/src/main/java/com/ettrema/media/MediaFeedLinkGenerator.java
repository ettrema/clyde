package com.ettrema.media;

import com.ettrema.media.MediaLogService.MediaType;

/**
 * Used by the MediaFeedResource to generate links to pages for media
 *
 * @author brad
 */
public interface MediaFeedLinkGenerator {
    String generateLink(MediaType mediaType, String mainContentPath);
}
