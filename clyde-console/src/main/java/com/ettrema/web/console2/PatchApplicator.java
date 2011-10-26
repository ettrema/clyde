package com.ettrema.web.console2;

import com.ettrema.web.Folder;
import com.ettrema.grid.Processable;


/**
  * marker interface
 */
public interface PatchApplicator extends Processable{
    String getName();
    
    void setArgs(String[] args);

    /**
     * Called by the Patch command to set the current folder
     *
     * @param currentResource
     */
    void setCurrentFolder( Folder currentResource );
}
