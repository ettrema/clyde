package com.bradmcevoy.web;

/**
 *
 * @author brad
 */
public interface AfterSavable {
    /**
     * Called after save is called on its page. 
     * 
     * @return - true if changes occured which must be saved
     */
    public boolean afterSave();
}
