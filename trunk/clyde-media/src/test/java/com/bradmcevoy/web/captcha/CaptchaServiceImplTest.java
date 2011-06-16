package com.bradmcevoy.web.captcha;

import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 *
 * @author brad
 */
public class CaptchaServiceImplTest {

    SystemTimeService systemTimeService;
    CaptchaServiceImpl impl;
    private static final Long CHALLENGE = Long.valueOf( 60 * 60 * 1000 + 1 );
    private static final String RESPONSE = "17173";

    @org.junit.Before
    public void setup() {
        systemTimeService = createMock( SystemTimeService.class );
        impl = new CaptchaServiceImpl( systemTimeService );
    }

    @Test
    public void testGetChallenge() {
        expect( systemTimeService.getCurrentTimeInMillis() ).andReturn( CHALLENGE );
        replay( systemTimeService );

        String s = impl.getChallenge();
        verify( systemTimeService );
        assertEquals( CHALLENGE.toString(), s );
    }

    @Test
    public void testValidateResponse() {
        expect( systemTimeService.getCurrentTimeInMillis() ).andReturn( Long.valueOf( 1 ) );
        replay( systemTimeService );

        boolean b = impl.validateResponse( CHALLENGE.toString(), RESPONSE );

        verify( systemTimeService );
        assertTrue( b );
    }

    @Test
    public void testGetResponse() {
        expect( systemTimeService.getCurrentTimeInMillis() ).andReturn( CHALLENGE );
        replay( systemTimeService );

        String resp = impl.getResponse( CHALLENGE.toString() );
        assertEquals( RESPONSE, resp );
    }

    @Test
    public void testIsValidChallenge() {
    }
}
