/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bradmcevoy.web.mail;

import com.ettrema.mail.StandardMessageImpl;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class VelocityTemplaterTest extends TestCase {

    VelocityTemplater templater;

    public VelocityTemplaterTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        templater = new VelocityTemplater();
    }



    public void testDoTemplating() {
        StandardMessageImpl sm = new StandardMessageImpl();
        sm.setText("my name is dataObject.name");
        sm.setHtml("my address is dataObject.address<br/>");
        User user = new User();
        templater.doTemplating(sm, user);
        assertEquals("my name is a_name", sm.getText());
        assertEquals("my address is 100 John St<br/>", sm.getHtml());
    }

    public class User {
        public String getName() {
            return "a_name";
        }

        public String getAddress() {
            return "100 John St";
        }
    }

}
