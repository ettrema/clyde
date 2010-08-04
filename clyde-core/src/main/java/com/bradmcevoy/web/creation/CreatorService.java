package com.bradmcevoy.web.creation;

import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.User;

/**
 *
 * @author brad
 */
public interface CreatorService {
    User getCreator(BaseResource res);

    void setCreator(User user, BaseResource res);
}
