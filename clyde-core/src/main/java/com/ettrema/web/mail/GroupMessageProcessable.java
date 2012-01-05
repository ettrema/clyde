package com.ettrema.web.mail;

import com.ettrema.web.ClydeStandardMessage;
import com.ettrema.context.Context;
import com.ettrema.grid.Processable;
import com.ettrema.mail.MailboxAddress;
import com.ettrema.mail.send.MailSender;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 *
 */
public class GroupMessageProcessable implements Processable, Serializable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GroupMessageProcessable.class);

    private static final long serialVersionUID = 1L;

    final UUID msgNameNodeId;
    final MailboxAddress groupAddress;
    final MailboxAddress from;
    final Map<MailboxAddress,UUID> mapOfMembers;

    public GroupMessageProcessable(UUID msgNameNodeId, MailboxAddress groupAddress, MailboxAddress from, Map<MailboxAddress,UUID> mapOfMembers) {
        if( msgNameNodeId == null ) throw new IllegalArgumentException("name node id is null");
        if( groupAddress == null ) throw new  IllegalArgumentException("groupAddress is null");
        if( from == null ) throw new  IllegalArgumentException("from is null");
        if( mapOfMembers == null ) throw new  IllegalArgumentException("mapOfMembers is null");
        this.msgNameNodeId = msgNameNodeId;
        this.groupAddress = groupAddress;
        this.from = from;
        this.mapOfMembers = mapOfMembers;
    }
 
    @Override
    public void doProcess(Context context) {
        VfsSession vfs = context.get(VfsSession.class);
        NameNode nameNode = vfs.get(msgNameNodeId);
        if( nameNode == null ) {
            log.warn("Name node not found: " + msgNameNodeId);
            return;
        }
        ClydeStandardMessage msg = (ClydeStandardMessage) nameNode.getData();
        MailSender mailSender = context.get(MailSender.class);
        GroupMessageSender gms = context.get(GroupMessageSender.class);
        Templater templater = context.get(Templater.class);
        gms.sendMessageToGroup(msg, groupAddress, from, mapOfMembers, mailSender, templater);
    }

    @Override
    public void pleaseImplementSerializable() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
