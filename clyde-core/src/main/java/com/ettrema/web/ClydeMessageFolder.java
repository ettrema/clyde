
package com.ettrema.web;

import com.bradmcevoy.http.Resource;
import com.ettrema.mail.MessageFolder;
import com.ettrema.mail.MessageResource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ClydeMessageFolder implements MessageFolder {

    private final Folder folder;
    
    /**
     * Constructs a messagefolder to wrap the given physical folder
     * 
     * @param folder
     */
    public ClydeMessageFolder(Folder folder) {
        this.folder = folder;
    }

    @Override
    public Collection<MessageResource> getMessages() {
        List<MessageResource> list = new ArrayList<>();
        if( this.folder == null ) return list;
        for( Resource r : this.folder.getChildren() ) {
            if( r instanceof MessageResource ) list.add((MessageResource)r);
        }
        return list;
    }

    @Override
    public int numMessages() {
        return getMessages().size();
    }

    @Override
    public int totalSize() {
        int i = 0;
        for( MessageResource m : getMessages() ) {
            i+=m.getSize();
        }
        return i;
    }

    public Folder getFolder() {
        return folder;
    }
    
    public long getNumUnread() {
        if( this.folder == null ) {
            return 0;
        }
        long count = 0;
        for( Resource r : folder.getChildren() ) {
            if( r instanceof ClydeStandardMessage ) {
                ClydeStandardMessage csm = (ClydeStandardMessage) r;
                if( !csm.isRead()) {
                    count++;
                }
            }
        }
        return count;
    }
    

}
