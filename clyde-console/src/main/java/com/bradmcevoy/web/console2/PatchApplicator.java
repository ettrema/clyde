package com.bradmcevoy.web.console2;

import com.bradmcevoy.grid.Processable;

/**
  * marker interface
 */
public interface PatchApplicator extends Processable{
    String getName();
    void setArgs(String[] args);
}
