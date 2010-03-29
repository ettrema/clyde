package com.bradmcevoy.web;

import com.bradmcevoy.vfs.DataNode;
import com.bradmcevoy.vfs.NameNode;
import com.ettrema.mail.MailboxAddress;
import java.io.Serializable;
import java.util.UUID;

/**
 * Just encapsulates an email address. This allows efficient searching for
 * email addresses using VFS.
 *
 * The name of the name node will be the value of the email address
 *
 * @author brad
 */
public class EmailAddress implements DataNode, Serializable{
    private static final long serialVersionUID = 1L;

    private UUID id;

    private transient NameNode nameNode;

    public void setId( UUID uuid ) {
        this.id = uuid;
    }

    public UUID getId() {
        return id;
    }

    public void init( NameNode nn ) {
        this.nameNode = nn;
    }

    public void onDeleted( NameNode nn ) {
        
    }

    public MailboxAddress getAddress() throws IllegalArgumentException {
        return MailboxAddress.parse( nameNode.getName());
    }

}
