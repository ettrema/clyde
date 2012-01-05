
package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import junit.framework.TestCase;

public class TestContentTypeUtil extends TestCase {
    public void test() {
        String ct = "image/jpeg,image/pjpeg";
        Iterable<Path> list = ContentTypeUtil.splitContentTypeList(ct);
        int cnt = 0;
        for( Path p : list ) {
            System.out.println("p: " + p);
            cnt++;
        }
        assertEquals(3, cnt);
    }
}
