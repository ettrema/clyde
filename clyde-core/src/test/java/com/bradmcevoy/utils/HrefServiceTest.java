package com.bradmcevoy.utils;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Host;
import org.junit.Test;

import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

/**
 *
 * @author brad
 */
public class HrefServiceTest {

    RequestService requestService;
    HrefService hrefService;

    CommonTemplated resource;
    Host host;

    @org.junit.Before
    public void setup() {
        requestService = createMock( RequestService.class );
        hrefService = new HrefService( requestService );

        resource = createMock( CommonTemplated.class);
        host = createMock(Host.class);
    }

    @Test
    public void test_GetUrl_FromRequest_NullResource() {
        String href = hrefService.getUrl( null );
        assertEquals( "", href );
    }

    @Test
    public void test_GetUrl_FromRequest_HostResource() {

        String href = hrefService.getUrl( host );

        assertEquals( "/", href );
    }

    @Test
    public void test_GetUrl_FromRequest_ChildResource() {
//        expect(resource.getParent()).andReturn( host );
//        expect(resource.getName()).andReturn( "aPage" );
//        replay(resource);
//
//        String href = hrefService.getUrl( resource );
//
//        verify(resource);
//        assertEquals( "/aPage", href );
    }


}
