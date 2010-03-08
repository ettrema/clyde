package com.bradmcevoy.web.mail;

import com.ettrema.mail.MailboxAddress;
import com.ettrema.mail.StandardMessage;
import com.ettrema.mail.send.MailSender;
import java.util.Map;

/**
 *
 */
public interface GroupMessageSender {
    /**
     * 
     *
     * @param msg
     * @param groupAddress
     * @param discardSubjects
     * @param from
     * @param mapOfMembers
     * @param mailSender
     * @return
     * @throws java.lang.IllegalArgumentException
     * @throws java.lang.RuntimeException
     */
    String sendMessageToGroup(StandardMessage msg, MailboxAddress groupAddress, MailboxAddress from, Map<MailboxAddress,Object> mapOfMembers, MailSender mailSender, Templater templater) throws IllegalArgumentException, RuntimeException;
}
