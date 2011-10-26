package com.bradmcevoy.web.comments;

import com.bradmcevoy.web.IUser;

/**
 *
 * @author brad
 */
public class UserBeanImpl implements UserBean{

    private String name;
    private String href;
    private String photoHref;

    public UserBeanImpl( String name, String href, String photoHref ) {
        this.name = name;
        this.href = href;
        this.photoHref = photoHref;
    }
    
    public String getName() {
        return name;
    }

    public String getHref() {
        return href;
    }

    public String getPhotoHref() {
        return photoHref;
    }

}
