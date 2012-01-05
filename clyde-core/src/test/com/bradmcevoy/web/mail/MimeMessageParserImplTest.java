package com.bradmcevoy.web.mail;

import com.bradmcevoy.vfs.MemoryVfsProvider;
import com.bradmcevoy.vfs.VfsManager;
import com.bradmcevoy.vfs.VfsSession;
import com.ettrema.web.Folder;
import com.ettrema.web.RootFolder;
import java.util.Date;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class MimeMessageParserImplTest extends TestCase {

    MimeMessageParserImpl messageParserImpl;

    public MimeMessageParserImplTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        messageParserImpl = new MimeMessageParserImpl();
    }



    public void testParseAndPersist() throws Exception {
        MemoryVfsProvider provider = new MemoryVfsProvider();
        VfsSession vfs = new VfsSession(new VfsManager(provider));
        
        MimeMessage mm = new MimeMessage((Session)null);
        mm.setFrom(new InternetAddress("noone@nowhere.com"));
        mm.setSubject("a subject");
        mm.setContent("text content", "text/plain");

        RootFolder root = new RootFolder(provider.createRoot(vfs));
        Folder destFolder = new Folder(root, "mockFolder");
        destFolder.save();
        messageParserImpl.parseAndPersist(mm, destFolder, vfs);
    }

    public void testBuildName() {
        Date dt = new Date(1238028785437l);
        String name = messageParserImpl.buildMessageName("a subject", "from", dt, 100);
        assertEquals("2009_03_26_a%20subject_100", name);
    }
}
