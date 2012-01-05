package com.ettrema.web;

/**
 * Finds template instances
 *
 * @author brad
 */
public interface TemplateManager {
    ITemplate lookup( String templateName, Folder web );
}
