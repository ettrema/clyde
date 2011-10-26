package com.ettrema.web.captcha;

/**
 *
 * @author brad
 */
public class SystemTimeServiceImpl implements SystemTimeService {

    public Long getCurrentTimeInMillis() {
        return System.currentTimeMillis();
    }

}
