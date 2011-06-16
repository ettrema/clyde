package com.bradmcevoy.web.security;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author brad
 */
public class RegexPasswordValidationServiceTest {

    private RegexPasswordValidationService svc;

    @Before
    public void setup() {
        svc = new RegexPasswordValidationService();
    }

    @Test
    public void testCheckValidity() {
    }

    @Test
    public void testMatches() {
        assertFalse(svc.matches( svc.getDefaultRegex(), "aaaaaZZaa"));
        assertTrue(svc.matches( svc.getDefaultRegex(), "abcdef1"));
    }
}
