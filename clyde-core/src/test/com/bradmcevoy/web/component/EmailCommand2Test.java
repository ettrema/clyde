package com.bradmcevoy.web.component;

import com.bradmcevoy.http.FileItem;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.RenderContext;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class EmailCommand2Test extends TestCase {
    
    public EmailCommand2Test(String testName) {
        super(testName);
    }

    public void test() throws Exception {
        CommonTemplated container = new CommonTemplated() {

            @Override
            public String getName() {
                return "parent";
            }

            @Override
            public CommonTemplated getParent() {
                return null;
            }

            @Override
            public String getUniqueId() {
                return "aaaa";
            }

            @Override
            public String getRealm() {
                return "realm1";
            }

            @Override
            public Date getModifiedDate() {
                return new Date();
            }

            @Override
            public Date getCreateDate() {
                throw new UnsupportedOperationException( "Not supported yet." );
            }


           
        };
        EmailCommand2 cmd = new EmailCommand2(container, "cmd");
        RenderContext rc = new RenderContext( null, container, null, false );
        Map<String, FileItem> files = new HashMap<String, FileItem>();
//        cmd.send( rc, files );
    }

}
