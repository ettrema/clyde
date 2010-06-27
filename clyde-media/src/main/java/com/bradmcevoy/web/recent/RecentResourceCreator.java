package com.bradmcevoy.web.recent;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.IUser;
import com.bradmcevoy.web.Web;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class RecentResourceCreator {
    /**
     * Create a recent resource to represent the given resource which has been
     * created or updated
     *
     * If a recent resource exists for the same physical resource it should be
     * deleted.
     *
     * @param r
     * @return
     */
    public RecentResource create(BaseResource r) {
        Web web = r.getWeb();
        Folder folder = web.getRecentFolder(true);
        String recentName = r.getNameNodeId().toString();
        BaseResource existing = (BaseResource) folder.getChildResource(recentName);
        if( existing != null ) {
            existing.delete();
        }
        checkMaxRecent(folder);

        IUser currentUser;
        Auth auth = HttpManager.request().getAuthorization();
        if( auth != null && auth.getTag() != null && auth.getTag() instanceof IUser ) {
            currentUser = (IUser) auth.getTag();
        } else {
            currentUser = null;
        }
        RecentResource rr = new RecentResource(folder, r, currentUser);
        rr.save();
        return null;
    }

    /**
     * Check if the recent list exceeds getRecentSize(), if so, delete the earliest
     * record
     * 
     * @param recentFolder - the Recent folder
     */
    private void checkMaxRecent(Folder recentFolder) {
        List<? extends Resource> children = recentFolder.getChildren();
        if (children != null && children.size() > getMaxRecentSize()) {
            Date earliestDate = new Date(); // now
            RecentResource toDelete = null;
            for (Resource child : children) {
                if (child instanceof RecentResource) {
                    Date childMod = child.getModifiedDate();
                    if (childMod != null && childMod.before(earliestDate)) {
                        earliestDate = childMod;
                        toDelete = (RecentResource) child;
                    }
                }
            }
            if (toDelete != null) {
                toDelete.deleteNoTx();
            }
        }
    }

    public void onDelete(BaseResource r) {
        Web web = r.getWeb();
        Folder folder = web.getRecentFolder(true);
        String recentName = r.getNameNodeId().toString();
        BaseResource existing = (BaseResource) folder.getChildResource(recentName);
        if( existing != null ) {
            existing.delete();
        }

    }

    private int getMaxRecentSize() {
        return 100;
    }
}
