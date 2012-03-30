package com.ettrema.web.templates;

import com.ettrema.web.Folder;
import com.ettrema.web.ITemplate;

/**
 * Finds template instances
 *
 * @author brad
 */
public interface TemplateManager {
    ITemplate lookup( String templateName, Folder web );
}
