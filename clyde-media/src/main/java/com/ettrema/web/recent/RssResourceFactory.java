package com.ettrema.web.recent;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.Folder;
import com.ettrema.web.User;
import com.ettrema.web.Web;

/**
 * Not to be used with decorator pattern. To be used with
 * MultipleResourceFactory
 *
 * Supports generating RSS files on demand for any folder, and also for
 * generating change logs (in a RSS similar format) for owner resources (ie
 * users and webs)
 *
 *
 */
public class RssResourceFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RssResourceFactory.class);
    private String rssName = "rss.xml";
    private String changeLogName = "_changelog.xml";
    private final ResourceFactory wrapped;
    private final RecentManager recentManager;

    public RssResourceFactory(ResourceFactory wrapped, RecentManager recentManager) {
        this.recentManager = recentManager;
        this.wrapped = wrapped;
    }

    @Override
    public Resource getResource(String host, String sPath) throws NotAuthorizedException, BadRequestException {
        Path path = Path.path(sPath);
        if (path.getName().equals(rssName)) {
            Path parentPath = path.getParent();
            Resource parent = wrapped.getResource(host, parentPath.toString());
            if (parent instanceof Folder) {
                Folder folder = (Folder) parent;
                return new RssResource(folder, rssName, recentManager);
            } else {
                return null;
            }
        } else if (path.getName().equals(changeLogName)) {
            Path parentPath = path.getParent();
            Resource parent = wrapped.getResource(host, parentPath.toString());
            if (parent instanceof User || parent instanceof Web) { // must be an owner resource
                Folder folder = (Folder) parent;
                return new ChangeLogResource(folder, rssName, recentManager);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public String getChangeLogName() {
        return changeLogName;
    }

    public void setChangeLogName(String changeLogName) {
        this.changeLogName = changeLogName;
    }

    public String getRssName() {
        return rssName;
    }

    public void setRssName(String rssName) {
        this.rssName = rssName;
    }
}
