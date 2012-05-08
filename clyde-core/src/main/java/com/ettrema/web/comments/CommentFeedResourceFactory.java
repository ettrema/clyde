package com.ettrema.web.comments;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.logging.LogUtils;
import com.ettrema.web.BaseResource;
import com.ettrema.web.LinkedFolder;

/**
 *
 * @author brad
 */
public class CommentFeedResourceFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CommentFeedResourceFactory.class);
    private String feedName = "_comments";
    private final ResourceFactory wrapped;
    private final CommentDao commentDao;
    private Long cacheSeconds;
    private boolean secure;

    public CommentFeedResourceFactory(ResourceFactory wrapped, CommentDao commentDao) {
        this.wrapped = wrapped;
        this.commentDao = commentDao;
    }

    @Override
    public Resource getResource(String host, String sPath) throws NotAuthorizedException, BadRequestException {
        LogUtils.trace(log,"getResource: ", sPath);
        Path path = Path.path(sPath);
        if (path.getName().equals(feedName)) {
            log.trace("got media feed name");
            Resource parent = wrapped.getResource(host, path.getParent().toString());
            if (parent instanceof BaseResource) {
                BaseResource baseResource = (BaseResource) parent;
                if( baseResource instanceof LinkedFolder ) {
                    LinkedFolder lf = (LinkedFolder) baseResource;
                    baseResource = lf.getLinkedTo();
                }
                String basePath = buildBasePath(host, path.getParent());
                log.trace("got media feed resource");
                return new CommentFeedResource(commentDao, feedName, baseResource, cacheSeconds, basePath);
            } else {
                log.trace("did not find: " + path.getParent());
                return null;
            }
        } else {
            log.trace("not media feed name");
            return null;
        }
    }

    public Long getCacheSeconds() {
        return cacheSeconds;
    }

    public void setCacheSeconds(Long cacheSeconds) {
        this.cacheSeconds = cacheSeconds;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    private String buildBasePath(String host, Path parent) {
        String prot = secure ? "https" : "http";
        String s = prot + "://" + host + parent.toString();
        log.debug("base path: " + s);
        return s;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }   
}
