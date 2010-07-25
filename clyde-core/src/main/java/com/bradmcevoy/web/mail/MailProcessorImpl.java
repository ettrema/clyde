package com.bradmcevoy.web.mail;

import com.bradmcevoy.web.ClydeStandardMessage;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.User;
import com.ettrema.context.RequestContext;
import com.ettrema.grid.AsynchProcessor;
import com.ettrema.mail.MailboxAddress;
import com.ettrema.mail.send.MailSender;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 */
public class MailProcessorImpl implements MailProcessor {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MailProcessorImpl.class);

    private final Map<String,Date> mapOfRecentEmails = new ConcurrentHashMap<String,Date>();

    @Override
    public void handleGroupEmail(MimeMessage mm, Folder destFolder, RequestContext context, List<User> members, MailboxAddress groupAddress, String discardSubjects) {
        AsynchProcessor asynchProcessor = context.get(AsynchProcessor.class);
        MailSender mailSender = context.get(MailSender.class);
        MimeMessageParser parser = context.get(MimeMessageParser.class);
        handleGroupEmail(mm, destFolder, mailSender, members, groupAddress, discardSubjects, asynchProcessor, parser);
    }

    public void handleGroupEmail(MimeMessage mm, Folder destFolder, MailSender mailSender, List<User> members, MailboxAddress groupAddress, String discardSubjects, AsynchProcessor asynchProcessor, MimeMessageParser parser) {
        if (destFolder == null) {
            throw new IllegalArgumentException("destination folder is null");
        }
        if(isAutoReply(mm)) {
            log.warn("DISCARDING autoreply");
            return ;
        }

        if( isDiscarded(discardSubjects, mm)) {
            log.warn("DISCARDING email");
            return ;
        }
        if( isRecent(mm)) {
            log.warn("discarding previously sent message");
            String add = groupAddress.user + "@" + groupAddress.domain;
            sendDiscardedMessage(mm, "Group email not sent", "You have recently sent a message with the same subject", mailSender, add);
            return ;
        }
        ClydeStandardMessage msg = parser.parseAndPersist(mm, destFolder);
        if( msg == null ) {
            throw new RuntimeException("failed to persist, message cannot be sent");
        } else {
            MailboxAddress from = msg.getFrom();
            if( from == null ) throw new RuntimeException("from is null");
            Map<MailboxAddress, Object> mapOfMembers = getMapOfMembers(members);
            GroupMessageProcessable gms = new GroupMessageProcessable(msg.getNameNodeId(), groupAddress, from, mapOfMembers);
            asynchProcessor.enqueue(gms);
        }
    }

    @Override
    public void forwardEmail(MimeMessage mm, String emailRecip, RequestContext context) {
        MailSender mailSender = context.get(MailSender.class);
        forwardEmail(mm, emailRecip, mailSender);
    }


    public void forwardEmail(MimeMessage mm, String emailRecip, MailSender mailSender) {
        try {
            MimeMessage m2 = mailSender.newMessage(mm);
            m2.setReplyTo(mm.getFrom());
            Address from;
            Address[] froms = mm.getFrom();
            if (froms != null && froms.length > 0) {
                from = froms[0];
            } else {
                log.warn("email without any from addresses???");
                from = null;
            }
            if (from != null) {
                m2.setFrom(from);
            }
            m2.setRecipient(RecipientType.TO, new InternetAddress(emailRecip));
            mailSender.sendMail(m2);
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ClydeStandardMessage persistEmail(MimeMessage mm, Folder destFolder, RequestContext context) {
        MimeMessageParser parser = context.get(MimeMessageParser.class);
        return parser.parseAndPersist(mm, destFolder);
    }




    InternetAddress getFrom(MimeMessage mm) {
        Address[] arr;
        try {
            arr = mm.getFrom();
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
        if (arr == null || arr.length == 0) {
            return null;
        }
        Address add = arr[0];
        if (add instanceof InternetAddress) {
            InternetAddress ia = (InternetAddress) add;
            return ia;
        } else {
            return null;
        }
    }

    boolean isDiscarded(String discardSubjects, MimeMessage mm) {        
        if( discardSubjects == null ) return false;
        String subject = getSubject(mm);
        log.debug("isDiscarded: " + discardSubjects + " == " + subject);
        String[] arr = discardSubjects.split(",");
        for( String discard : arr ) {
            if( discard.trim().length() > 0 ) {
                if( subject.contains(discard)) return true;
            }
        }
        return false;
    }

    String getSubject(MimeMessage mm) {
        try {
            return mm.getSubject();
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }

    Map<MailboxAddress, Object> getMapOfMembers(List<User> members) {
        Map<MailboxAddress, Object> map = new HashMap<MailboxAddress, Object>();
        for( User u : members) {
            String email = u.getExternalEmailText();
            if( email != null ) {
                try {
                    MailboxAddress toAdd = MailboxAddress.parse(email);
                    map.put(toAdd, u);
                } catch (IllegalArgumentException e) {
                    log.debug("Couldnt parse email address: " + email + "   : " + e.getMessage());
                }
            }
        }
        return map;
    }

    boolean isAutoReply(MimeMessage mm) {
        String sub = getSubject(mm);
        return isAutoReply(sub);
    }

    boolean isAutoReply(String sub) {
        sub = sub.toLowerCase();
        if( sub.contains("autoreply") ) return true;
        if( sub.contains("out of") && sub.contains("office") ) return true;
        return false;
    }

    boolean isRecent(MimeMessage mm) {
        String subject = getSubject(mm);
        InternetAddress iaFrom = getFrom(mm);
        if (iaFrom == null) {
            throw new RuntimeException("Unsupported from format");
        }
        String sFromAddress = iaFrom.getAddress();

        String key = sFromAddress + "_" + subject;
        Date lastSent = mapOfRecentEmails.get(key);
        if( lastSent == null ) {
            // not sent, so record this message
            Date date = new Date();
            mapOfRecentEmails.put(key, date);
            return false;
        } else {
            log.warn("message with key: " + key + "  was last sent at: " + lastSent);
            return true;
        }

    }


    void sendDiscardedMessage(MimeMessage mmDiscarded, String subject, String message, MailSender mailSender, String groupAddress) {
        try {
            InternetAddress originatorAddress = (InternetAddress) mmDiscarded.getFrom()[0];
            String fromPersonal = "Group Manager";
            String fromAddress = groupAddress;
            List<String> to = new ArrayList<String>();
            to.add(originatorAddress.getAddress());
            String replyTo;
            mailSender.sendMail(fromAddress, fromPersonal, to, null, subject, message);
        } catch (AddressException ex) {
            throw new RuntimeException(ex);
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }



//    private void sendSmsToUser(String groupAddress, String mobile, String newContent, RequestContext context) {
//        String smsToTemplate = context.get("sms.to.template");
//        if( smsToTemplate == null || smsToTemplate.length() == 0 ) throw new IllegalArgumentException("No sms.to.template speficied");
//        String smsToAddress = smsToTemplate.replace("$mob", mobile);
//        log.debug("sending sms to: " + smsToAddress);
//        List<String> toList = new ArrayList<String>();
//        toList.add(smsToAddress);
//        MailSender mailSender = context.get(MailSender.class);
//        mailSender.sendMail(groupAddress, null, toList, groupAddress, "", newContent);
//    }


}
