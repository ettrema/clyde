package com.ettrema.web.mail;

import com.bradmcevoy.http.Utils;
import com.ettrema.web.ClydeStandardMessage;
import com.ettrema.web.Folder;
import com.ettrema.context.Context;
import com.ettrema.context.RequestContext;
import com.ettrema.mail.StandardMessageFactoryImpl;
import com.ettrema.vfs.VfsSession;
import com.ettrema.vfs.VfsTransactionManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 */
public class MimeMessageParserImpl implements MimeMessageParser{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MimeMessageParserImpl.class);

    @Override
    public ClydeStandardMessage parseAndPersist(MimeMessage mm, Folder destFolder) {
        Context context = RequestContext.getCurrent();
        VfsSession vfs = context.get(VfsSession.class);
        return parseAndPersist(mm, destFolder, vfs);
    }

    ClydeStandardMessage parseAndPersist(MimeMessage mm, Folder destFolder, VfsSession vfs) {
        String nm = buildMessageName(mm);
        log.debug("saving message: " + nm);
        nm = destFolder.buildNonDuplicateName(nm);
        ClydeStandardMessage sm = new ClydeStandardMessage(destFolder, nm);
        StandardMessageFactoryImpl smf = new StandardMessageFactoryImpl();
        smf.toStandardMessage(mm, sm);
        try {
            sm.save();
            VfsTransactionManager.commit();
            log.debug("saved to: " + sm.getNameNodeId());
            return sm;
        } catch (Throwable e) {
            log.error("exception saving message: " + sm.getSubject(), e);
            VfsTransactionManager.rollback();
            return null;
        }        
    }

    String sanitiseMessageName(String newName) {
        return Utils.percentEncode(newName);
    }

    String buildMessageName(MimeMessage mm) {
        try {
            String subject = mm.getSubject();
            String from = ((InternetAddress) mm.getFrom()[0]).getAddress();
            Date dt = new Date();
            return buildMessageName(subject, from, dt);
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }

    String buildMessageName(String subject, String from, Date dt) {
        int rnd = (int) (Math.random() * 1000);
        return buildMessageName(subject, from, dt, rnd);
    }

    String buildMessageName(String subject, String from, Date dt, int rnd) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
        String s = sdf.format(dt);
        s = s + "_" + subject;
        s = sanitiseMessageName(s);        
        s = s + "_" + rnd;
        return s;
    }
  

}
