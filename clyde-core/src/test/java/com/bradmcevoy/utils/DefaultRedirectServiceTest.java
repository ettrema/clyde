package com.bradmcevoy.utils;

import com.bradmcevoy.web.BaseResource;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.easymock.classextension.EasyMock.*;

/**
 *
 * @author brad
 */
public class DefaultRedirectServiceTest {

    DefaultRedirectService defaultRedirectService;

    @Before
    public void setUp() {
        defaultRedirectService = new DefaultRedirectService();
    }

    @Test
    public void testCheckRedirect_NullResource() {

        // BaseResource r = (BaseResource) res;
        //String redirect = r.getRedirect();

        // Request request = null;
        // expect(request.getAbsoluteUrl()).andReturn( "http://blahlbahblah.com");
        // replay(request);

        String result = defaultRedirectService.checkRedirect( null, null );

        // verify(request);
        assertEquals( null, result );
    }

    @Test
    public void testCheckRedirect_BaseResource_WithRedirect() {

        BaseResource r = createMock(BaseResource.class);
        //String redirect = r.getRedirect();

        // Request request = null;
        expect(r.getRedirect()).andReturn("hello");
        replay(r);

        String result = defaultRedirectService.checkRedirect( r, null );

         verify(r);
        assertEquals( "hello", result );
    }
}
