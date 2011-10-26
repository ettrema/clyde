package com.ettrema.web.security;

import com.ettrema.web.IUser;

/**
 * Represents a means to find out what user is responsible for the current action
 *
 * Ie the user requesting a page request, or the user responsible for a background
 * process.
 *
 * There are two types of current user:
 *  - the user to consider for security purposes
 *  - the user who is the subject of the current operation
 *
 * Code should call the appropriate method based on whether they are testing
 * for security (ie to allow or not) or if they are looking up information
 * logically associated with the user (the to set a name on something)
 *
 * @author brad
 */
public interface CurrentUserService {

    /**
     * This is the user to consider when determining whether or not to allow
     * certain actions. In the case of a background process, this user will
     * be the privildeged user executing the process, not the customer user
     * who is the subject of the process.
     * 
     * In the case of a normal web request this will be the authenticated user
     * making the request
     * 
     * Or null, if there is no particular user context, eg an anonymous web request
     * 
     * @return
     */
    IUser getSecurityContextUser();

    /**
     * Where the getSecurityContextUser returns an elevated user which is operating
     * on another subject, this method returns the subject.
     *
     * This will be the same as getSecurityContextUser for normal page requests.
     *
     * @return
     */
    IUser getOnBehalfOf();

    /**
     * Makes the given user the subject of the current operation.
     *
     * @param user - the user to make current
     */
    void setOnBehalfOf(IUser user);

}
