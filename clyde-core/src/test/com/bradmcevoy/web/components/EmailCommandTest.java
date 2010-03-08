package com.bradmcevoy.web.components;

import com.bradmcevoy.AbstractTest;
import com.bradmcevoy.vfs.MemoryNameNode;
import com.bradmcevoy.vfs.MemoryVfsProvider;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Page;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.RootFolder;
import com.bradmcevoy.web.component.EmailCommand;
import com.bradmcevoy.web.component.Text;

public class EmailCommandTest extends AbstractTest {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EmailCommandTest.class);
    
    Page page;
    EmailCommand c;
    RenderContext rc;

    public EmailCommandTest() {
        super("emailcommandtest");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MemoryVfsProvider provider = new MemoryVfsProvider();
        MemoryNameNode node = new MemoryNameNode(provider, "root", null, null);
        Folder folder = new RootFolder(node);
        page = new Page(folder, "testpage");
        page.getComponents().add(new Text(page, "message"));
        c = new EmailCommand(page, "send");
        page.getComponents().add(c);
        c.getTemplate().setValue("Test body: @{targetPage.getComponent('message').value}");
        rc = new RenderContext(null, page, null, false);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        c = null;
        rc = null;
    }

    public void testRender() {
        String s = c.render(rc);
        log.debug("render: " + s);
        assertEquals("<input type='submit' name='send' value='send' />", s);
    }

//    public void testTemplate() {
//        Text t = (Text) rc.getTargetPage().getComponent("message");
//        assertNotNull(t);
//        t.setValue("test message");
//        String s = c.getEmailBody(rc);
//        log.debug("template: " + s);
//        assertEquals("Test body: test message", s);
//    }

//    public void testSend() {
//        TestUtils.runTest(new Executable() {
//
//                    public Object execute(Context context) {
//                        try {
//                            Text t = (Text) rc.getTargetPage().getComponent("message");
//                            assertNotNull(t);
//                            t.setValue("test message");
//                            c.getSubject().setValue("test subject");
//                            c.getFrom().setValue("brad@bradmcevoy.com");
//                            c.getTo().setValue("brad@bradmcevoy.com");
//
////                            c.send(rc);
//                        } catch (MessagingException ex) {
//                            ex.printStackTrace();
//                            fail();
//                        }
//                        return null;
//                    }
//                });
//    }
//
//    public void testPersist() throws SAXParseException {
//        c.getSubject().setValue("test subject");
//        c.getFrom().setValue("brad@bradmcevoy.com");
//        c.getTo().setValue("brad@bradmcevoy.com");
//
//        XmlUtils2 utils = new XmlUtils2();
//        Document doc = utils.getDomDocument("<test></test>");
//        Element el = doc.getDocumentElement();
//  //      c.toXml(el);
//        
//        String s = utils.getXml(doc);
//        log.debug("xml:  " + s);
//        
//        el = (Element) el.getFirstChild();
//        EmailCommand cmd = (EmailCommand) XmlUtils2.restoreObject(el, page);
//        assertNotNull(cmd);
//    }
    
    
}
