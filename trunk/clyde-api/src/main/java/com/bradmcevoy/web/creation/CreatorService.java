package com.bradmcevoy.web.creation;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.IUser;

/**
 * Service to set the creator on a new resource, and to return the creator
 * of the service
 *
 * @author brad
 */
public interface CreatorService {
    IUser getCreator(Resource res);

    void setCreator(IUser user, Resource res);
}
