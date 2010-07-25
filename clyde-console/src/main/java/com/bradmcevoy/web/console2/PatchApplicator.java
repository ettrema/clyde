package com.bradmcevoy.web.console2;

import com.ettrema.grid.Processable;


/**
  * marker interface
 */
public interface PatchApplicator extends Processable{
    String getName();
    void setArgs(String[] args);
}
