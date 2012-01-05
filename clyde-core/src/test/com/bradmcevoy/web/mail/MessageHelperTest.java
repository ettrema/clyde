package com.bradmcevoy.web.mail;

import com.ettrema.web.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class MessageHelperTest extends TestCase {

    public MessageHelperTest(String testName) {
        super(testName);
    }

    public void testFormatHtml() {
        String s = "<DIV><FONT face=Arial size=2></FONT>&nbsp;</DIV>\n";
        s = s + "<DIV><IMG alt='' hspace=0 src='cid:abc'\n";
        s = s + "align=baseline border=0></DIV>";

        ClydeStandardMessage msg = new ClydeStandardMessage(new RootFolder(), "msg");
        List<Templatable> list = new ArrayList<Templatable>();
        EmailAttachment ea = new MockEmailAttachment(msg, "", "", "abc");
        list.add(ea);

        String html = MessageHelper.formatHtml(s, list);
        System.out.println(html);
        assertTrue(html.contains("src='/mock/href'"));
    }

    public void testStripLeadingBody() {
        String s = "!!!!<BODY class='aaa'>some content";
        assertEquals("some content", MessageHelper.stripLeadingBody(s));
    }

    public void testParseContentIds() {
        String s = "<DIV><FONT face=Arial size=2></FONT>&nbsp;</DIV>\n";
        s = s + "<DIV><IMG alt='' hspace=0 src='cid:12ABF7669AC747EB8F7FF115D9D46578@bradsalien'\n";
        s = s + "align=baseline border=0></DIV>";
        Set<String> set = MessageHelper.parseContentIds(s);
        assertEquals(1, set.size());
        assertEquals("cid:12ABF7669AC747EB8F7FF115D9D46578@bradsalien", set.iterator().next());
    }

    public void testGetAttachmentsMap() {
        ClydeStandardMessage msg = new ClydeStandardMessage(new RootFolder(), "msg");
        List<Templatable> list = new ArrayList<Templatable>();
        EmailAttachment ea = new EmailAttachment(msg, "", "", "abc");
        list.add(ea);
        CommonTemplated ct = new TextFile(msg, "");
        list.add(ct);
        Map<String, EmailAttachment> map = MessageHelper.getAttachmentsMap(list);
        assertEquals(1, map.size());
        assertEquals(ea, map.get("cid:abc"));
    }

    class MockEmailAttachment extends EmailAttachment {

        public MockEmailAttachment(ClydeStandardMessage parent, String contentType, String name, String contentId) {
            super(parent, contentType, name, contentId);
        }

        @Override
        public String getHref() {
            return "/mock/href";
        }


    }
}
