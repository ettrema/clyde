
package com.bradmcevoy.web;

import junit.framework.TestCase;

public class NameAndAuthorityTest extends TestCase{
    public void test() {
        String t1 = "blah@domain";
        NameAndAuthority na = NameAndAuthority.parse(t1);
        assertNotNull(na);
        assertEquals("blah", na.name);
        assertEquals("domain", na.authority);

        String t2 = "blah";
        na = NameAndAuthority.parse(t2);
        assertNotNull(na);
        assertEquals("blah", na.name);
        assertNull(na.authority);
    }
}
