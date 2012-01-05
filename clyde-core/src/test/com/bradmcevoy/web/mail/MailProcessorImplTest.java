package com.bradmcevoy.web.mail;

import com.bradmcevoy.grid.AsynchProcessor;
import com.bradmcevoy.grid.MockAsynchProcessor;
import com.ettrema.web.ClydeStandardMessage;
import com.ettrema.web.Folder;
import com.ettrema.web.RootFolder;
import com.ettrema.web.User;
import com.ettrema.mail.MailboxAddress;
import com.ettrema.mail.send.MailSender;
import com.ettrema.mail.send.MockMailSender;
import com.sun.mail.smtp.SMTPMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

/**
 *
 */
public class MailProcessorImplTest extends TestCase{
    private MailProcessorImpl proc;
    private InternetAddress from;

    @Override
    protected void setUp() throws Exception {
        proc = new MailProcessorImpl();
        from = new InternetAddress("noone@nowhere.com");
    }

    public void testIsRecent() throws Exception {
        MimeMessage mm = new SMTPMessage((Session)null);
        mm.setSubject("a subject");
        mm.setFrom(from);
        assertFalse( proc.isRecent(mm) );
        // sent again should return true
        assertTrue( proc.isRecent(mm) );
        mm.setSubject("b subject");
        assertFalse( proc.isRecent(mm) );
    }

    public void testSendDiscarded() throws Exception {
        MimeMessage mmDiscarded = new SMTPMessage((Session)null);
        mmDiscarded.setFrom(from);
        String subject = "discarded";
        String message = "discarded message";
        MockMailSender mailSender = new MockMailSender();
        String groupAddress = "groupadd";
        proc.sendDiscardedMessage(mmDiscarded, subject, message, mailSender, groupAddress);
        assertEquals(1, mailSender.getSentMessages().size());
        MockMailSender.SentMessage msg = mailSender.getSentMessages().get(0);
        assertEquals(groupAddress, msg.fromAddress);
        assertEquals(subject, subject);
        assertEquals(message, message);
        assertEquals(1, msg.to.size());
        assertEquals(from.getAddress(), msg.to.get(0));
    }

    public void testIsAutoReply() {
        assertTrue(proc.isAutoReply("autoreply"));
        assertTrue(proc.isAutoReply("i am an Autoreply"));
        assertTrue(proc.isAutoReply("sorry i am out of the office"));
        assertTrue(proc.isAutoReply("Out of office: back tomorrow"));
        assertFalse(proc.isAutoReply(""));
        assertFalse(proc.isAutoReply("a"));
        assertFalse(proc.isAutoReply("the office"));
        assertFalse(proc.isAutoReply("out of time"));
    }

    public void testIsDiscarded() throws Exception {
        MimeMessage mm = new SMTPMessage((Session)null);
        mm.setFrom(from);
        mm.setSubject("a subject");
        assertTrue(proc.isDiscarded("a subject,b subject", mm));
        assertFalse(proc.isDiscarded("b subject", mm));
    }

    public void testHandleGroupEmail() throws Exception {
        MimeMessage mm = new SMTPMessage((Session)null);
        mm.setFrom(from);
        mm.setSubject("a subject");

        RootFolder root = new RootFolder();
        Folder destFolder = new Folder(root, "mockFolder");

        MailSender mailSender = new MockMailSender();

        List<User> members = new ArrayList<User>();
        User member = new User(destFolder, "user1");
        member.setExternalEmailText("user1@somewhere.com");
        members.add(member);

        member = new User(destFolder, "user2");
        member.setExternalEmailText(null);
        members.add(member);

        MailboxAddress groupAddress = new MailboxAddress("agroup", "groups.com");
        String discardedSubjects = null;

        MockAsynchProcessor asynchProcessor = new MockAsynchProcessor();
        MimeMessageParser parser = createMock(MimeMessageParser.class);
        ClydeStandardMessage csm = new ClydeStandardMessage(destFolder, "aname");
        csm.setFrom(new MailboxAddress("from", "from.com"));
        expect(parser.parseAndPersist(mm, destFolder)).andReturn(csm);
        replay(parser);
        proc.handleGroupEmail(mm, destFolder, mailSender, members, groupAddress, discardedSubjects, asynchProcessor, parser);

        GroupMessageProcessable p = (GroupMessageProcessable) asynchProcessor.queue.get(0);
        assertEquals(csm.getFrom(), p.from);
        assertEquals("agroup", p.groupAddress.user);
        assertEquals("groups.com", p.groupAddress.domain);
        assertEquals(1,p.mapOfMembers.size());        
    }

    public void testGetGroupMembers() {
        RootFolder root = new RootFolder();
        List<User> members = new ArrayList<User>();
        User u = new User(root, "a");
        u.setExternalEmailText("blah blah com");
        members.add(u);
        u = new User(root,"b");
        u.setExternalEmailText("real@address.com");
        members.add(u);
        Map<MailboxAddress, Object> map = proc.getMapOfMembers(members);
        assertEquals(1, map.size());
    }

}
