package com.ettrema.underlay;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.logging.LogUtils;
import com.ettrema.web.*;

/**
 * Finds resources by searching through underlays
 *
 * @author brad
 */
public class UnderlayResourceFactory extends CommonResourceFactory{


    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UnderlayResourceFactory.class);

    private final UnderlayLocator underlayLocator;
    
    public UnderlayResourceFactory(HostFinder hostFinder, UnderlayLocator underlayLocator) {
        super(hostFinder);
        this.underlayLocator = underlayLocator;
    }    
    
    @Override
    public Resource getResource(String host, String url) throws NotAuthorizedException, BadRequestException {
        String sPath = url;
        Path path = Path.path(sPath);
        if (host != null && host.contains(":")) {
            host = host.substring(0, host.indexOf(":"));
        }
        Host theHost = getHost(host);
        
        Resource r = findUnderlayResource(theHost, path);
        if( r instanceof ISubPage ) {
            ISubPage subPage = (ISubPage) r;
            // HACK: theHost is not necessarily the direct parent, since the actual resource might be under /templates etc
            // But i think this might be ok for templating purposes
            r = new WrappedSubPage(subPage, theHost); 
        }
        LogUtils.trace(log, "getResource: resource=", r);
        return r;
    }
    
    public Resource findUnderlayResource(Host theHost, final Path path) throws NotAuthorizedException, BadRequestException {
        Resource r = UnderlayUtils.walkUnderlays(theHost, underlayLocator, new UnderlayUtils.UnderlayVisitor<Resource>() {

            @Override
            public Resource visitUnderlay(Host underLayFolder) throws NotAuthorizedException, BadRequestException{
                Resource r = ExistingResourceFactory.findChild(underLayFolder, path);
                if( r != null ) {
                    LogUtils.trace(log, "findUnderlayResource-visitUnderlay: found resource from underlay", underLayFolder.getName(), "path", path);
                } else {
                    LogUtils.trace(log, "findUnderlayResource-visitUnderlay: did not find resource from underlay", underLayFolder.getName(), "path", path);
                }
                return r;
            }
        });
        return r;
    }    


    
    
}
