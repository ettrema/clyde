package com.bradmcevoy.web.mail;

import com.ettrema.mail.MailboxAddress;
import com.ettrema.mail.StandardMessage;
import com.ettrema.mail.StandardMessageFactoryImpl;
import com.ettrema.mail.StandardMessageWrapper;
import com.ettrema.mail.send.MailSender;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static com.ettrema.context.RequestContext._;

/**
 *
 */
public class GroupMessageSenderImpl implements GroupMessageSender {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( GroupMessageSenderImpl.class );

    /**
     *
     * @param from
     * @param to
     * @return - true if the domain and user names are equals
     */
    boolean isSame( MailboxAddress from, MailboxAddress to ) {
        return ( from.domain.equals( to.domain ) ) && ( from.user.equals( to.user ) );
    }

    @Override
    public String sendMessageToGroup( StandardMessage msg, MailboxAddress groupAddress, MailboxAddress from, Map<MailboxAddress, UUID> mapOfMembers, MailSender mailSender, Templater templater ) throws IllegalArgumentException, RuntimeException {
        StringBuilder sbSentTo = new StringBuilder();
        log.debug( "clearing CC and BCC lists and headers" );
        msg.setCc( new ArrayList<MailboxAddress>() );
        msg.setBcc( new ArrayList<MailboxAddress>() );
        msg.setHeaders( new HashMap<String, String>() );
        log.debug( "storeMail. members: " + mapOfMembers.size() );
        MailboxAddress originalFrom = from;
        msg.setReplyTo( originalFrom );
        String fromName = from.personal;
        MailboxAddress hybridFrom = new MailboxAddress( groupAddress.user, groupAddress.domain, fromName );
        msg.setFrom( hybridFrom );
        for( Map.Entry<MailboxAddress, UUID> recipEntry : mapOfMembers.entrySet() ) {
            MailboxAddress to = recipEntry.getKey();
            UUID userId = recipEntry.getValue();
            try {
                boolean isSame = isSame( from, to ); // sRecipEmailAddress.equalsIgnoreCase(sFromAddress);
                if( !isSame ) {
                    DataNode dataObject = findDataObject(userId);
                    sendGroupMessageToUser( to, dataObject, msg, mailSender, templater );
                    sbSentTo.append( to.toString() ).append( ":" );
                } else {
                    log.debug( "not sending it to original sender: " + to.toString() );
                }
            } catch( Error error ) {
                log.error( "ERROR processing mail. Aborting", error );
                break;
            } catch( Throwable ex ) {
                log.error( "Exception sending email to: " + to, ex );
            }
        }
        return sbSentTo.toString();
    }

    private void sendGroupMessageToUser( MailboxAddress to, Object u, StandardMessage groupMsg, MailSender mailSender, Templater templater ) {
        log.debug( "sending to: " + to );
        StandardMessage userMsg = new StandardMessageWrapper( groupMsg );
        templater.doTemplating( userMsg, u );
        List<MailboxAddress> toList = new ArrayList<MailboxAddress>();
        toList.add( to );
        userMsg.setTo( toList );

//        String subject = groupMsg.getSubject();
//        String groupAddress = groupMsg.getFrom().toString();
//        if( subject != null && subject.startsWith("sms:")) {
//            String mobile = u.getMobileNumber();
//            if( mobile != null && mobile.length() > 0 ) {
//                sendSmsToUser(groupAddress, mobile, groupMsg.getText(), context);
//                return ;
//            }
//        }

        MimeMessage m2 = mailSender.newMessage();
        StandardMessageFactoryImpl factory = new StandardMessageFactoryImpl();
        factory.toMimeMessage( userMsg, m2 );
        checkMessage( m2 );
        mailSender.sendMail( m2 );

    }

    private void checkMessage( MimeMessage m ) {
        try {
            Address[] arr = m.getAllRecipients();
            if( arr.length != 1 ) {
                log.warn( "Too many recipients: " + arr.length );
                for( Address a : arr ) {
                    log.warn( "  recip: " + a.toString() );
                }
            }
        } catch( MessagingException ex ) {
            throw new RuntimeException( ex );
        }
    }

    private DataNode findDataObject( UUID userId ) {
        NameNode nn = _(VfsSession.class).get( userId );
        if( nn == null ) {
            return null;
        } else {
            return nn.getData();
        }
    }
}
