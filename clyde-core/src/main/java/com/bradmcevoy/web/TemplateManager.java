package com.bradmcevoy.web;

/**
 * Finds template instances
 *
 * @author brad
 */
public interface TemplateManager {
    ITemplate lookup( String templateName, Web web );
}
