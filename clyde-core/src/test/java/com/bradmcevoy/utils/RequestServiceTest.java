package com.bradmcevoy.utils;

import com.bradmcevoy.http.Request;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.easymock.classextension.EasyMock.*;

/**
 *
 * @author brad
 */
public class RequestServiceTest {

    RequestService requestService;
    CurrentRequestService currentRequestService;

    @Before
    public void setUp() {
        currentRequestService = createMock( CurrentRequestService.class );
        requestService = new RequestService( currentRequestService );
    }

    @Test
    public void testIsSecure() {

    }

    @Test
    public void testIsSecure_Request() {
        Request request = createMock(Request.class);
        expect(request.getAbsoluteUrl()).andReturn( "http://blahlbahblah.com");
        replay(request);

        boolean result = requestService.isSecure( request );

        verify(request);
        assertEquals(false, result);
    }
     @Test
    public void testIsSecure_Request_NULL() {
       Request request = null;
       // expect(request.getAbsoluteUrl()).andReturn( "http://blahlbahblah.com");
       // replay(request);

        boolean result = requestService.isSecure( request );

       // verify(request);
        assertEquals(false, result);
    }
     @Test
    public void testIsSecure_URL_Request_NULL() {
       Request request = createMock(Request.class);
        expect(request.getAbsoluteUrl()).andReturn( null );
        replay(request);

        boolean result = requestService.isSecure( request );

        verify(request);
        assertEquals(false, result);
    }
    @Test
    public void testIsSecure_HTTPS_Request_NULL() {
       Request request = createMock(Request.class);
       expect(request.getAbsoluteUrl()).andReturn( "https://yes.com" );
       replay(request);

        boolean result = requestService.isSecure( request );

        verify(request);
        assertEquals(true, result);
    }
}
