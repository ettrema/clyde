package com.ettrema.web.templates;

import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.logging.LogUtils;
import com.ettrema.underlay.UnderlayLocator;
import com.ettrema.underlay.UnderlayUtils;
import com.ettrema.web.Folder;
import com.ettrema.web.Host;
import com.ettrema.web.ITemplate;

/**
 * Locates templates by looking through underlays
 *
 * @author brad
 */
public class UnderlayTemplateManager implements TemplateManager {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UnderlayTemplateManager.class);
    
    private final UnderlayLocator underlayLocator;
    private final TemplateManager templateManager;

    public UnderlayTemplateManager(UnderlayLocator underlayLocator, TemplateManager templateManager) {
        this.underlayLocator = underlayLocator;
        this.templateManager = templateManager;
    }


    @Override
    public ITemplate lookup(final String templateName, final Folder folder) {
        // First try to get from wrapped template manager
        ITemplate template = templateManager.lookup(templateName, folder);
        if( template != null ) {
            LogUtils.trace(log, "lookup: found template from wrapped template manager");
            return template;
        }
        
        // Not directly on this web, so try to get from underlay, if the web is a host (web's dont support underlays)
        if (folder instanceof Host) {
            try {
                Host theHost = (Host) folder;
                ITemplate r = UnderlayUtils.walkUnderlays(theHost, underlayLocator, new UnderlayUtils.UnderlayVisitor<ITemplate>() {

                    @Override
                    public ITemplate visitUnderlay(Host underLayFolder) throws NotAuthorizedException, BadRequestException {
                        ITemplate t = templateManager.lookup(templateName, underLayFolder);
                        if( t != null ) {
                            LogUtils.trace(log, "lookup: found template from underlay", underLayFolder.getName(), "web", folder.getWeb());
                            //return new WrappedTemplate(t, folder.getWeb());
                            return t;
                        } else {
                            LogUtils.trace(log, "lookup: did not find template from underlay", underLayFolder.getName());
                            return null;
                        }                        
                    }
                });                                
                return r;
            } catch (NotAuthorizedException | BadRequestException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            LogUtils.trace(log, "lookup: no template in wrapped, and target is not a host", folder.getName());
            return null; // maybe should go to the web's host?
        }
    }
}
