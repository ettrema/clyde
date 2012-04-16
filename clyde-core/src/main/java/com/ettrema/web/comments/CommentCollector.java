package com.ettrema.web.comments;

import java.util.Date;
import java.util.UUID;

/**
 *
 * @author brad
 */
public interface CommentCollector {

    /**
     * Return true to keep going, false aborts search
     *
     * @param nameId
     * @param datePosted
     * @param pagePath
     * @return
     */
    boolean onResult(UUID nameId, Date datePosted, String pagePath);
}
