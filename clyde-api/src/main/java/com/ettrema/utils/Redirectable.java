package com.ettrema.utils;

/**
 * Represents any resource which can redirect to another url
 *
 * This is typically a persisted redirect, such as when a host should redirect
 * to another
 *
 * This should not be used to implement a general redirect mechanism, use a
 * RedirectService instead
 *
 * @author brad
 */
public interface Redirectable {
    /**
     * Return the (usually persisted) redirect value for this resource
     *
     * @return
     */
    String getRedirect();
}
