package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.context.RequestContext;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsSession;
import java.util.UUID;

public class ExistingResourceFactory extends CommonResourceFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ExistingResourceFactory.class);

    @Override
    public Resource getResource(String host, String url) {
        String sPath = url;
        Path path = Path.path(sPath);

        return findPage(host, path);
    }

    public Resource findPage(String host, Path path) {
//        log.debug("findPage: " + path);
        if( host.contains( ":")) {
            host = host.substring( 0, host.indexOf( ":"));
        }
        Host theHost = getHost(host);
        if( theHost == null ) {
            log.error("host name not found: " + host);
            return null;
        }
        Resource r = findChild(theHost, path);
        return r;
    }    
    
    public static Resource findChild(Resource parent, Path path) {
        return findChild(parent, path.getParts(),0);
    }
    
    public static Resource findChild(Resource parent, String childSpec) {
//        log.debug("findChild: " + parent.getName() + " - " + childSpec);
        if( childSpec.equals(".") ) {
            return parent;
        } else if( childSpec.equals("..") ) {
            if( parent instanceof CommonTemplated ) {
                CommonTemplated ct = (CommonTemplated) parent;
                return ct.getParent();
            } else {
                log.warn("Can't find parent of non CommonTemplated resource");
                return null;
            }
        } else {            
            Resource child = null;
            if( parent instanceof CollectionResource ) {
                CollectionResource col = (CollectionResource) parent;                        
                child = col.child(childSpec);
                child = checkAndWrap(child, parent);
            }

            if( child == null && parent instanceof CommonTemplated ) {
                CommonTemplated t = (CommonTemplated) parent;
                child = t.getChildResource(childSpec); 
                child = checkAndWrap(child, parent);
            }
            
            return child;
        }        
    }
    
    public static Resource findChild(Resource parent, String[] arr, int i) {
        if( arr.length == 0 ) return parent;
//        log.debug("findChild: "  + parent.getName() + " - " + arr[i] + " - " + i);        

        String childName = arr[i];    
        Resource child = findChild(parent, childName);
        
        if( child == null ) {
            return null;
        } else {            
            if( i < arr.length-1) {
                return findChild(child, arr, i+1);
            } else {
                return child;
            }
        }
    }    
    
    static Resource checkAndWrap(Resource r, Resource parent) {
        if( r == null ) return null;
        
//        log.debug("checkAndWrap: " + r.getHref());
        Resource r2;        
        if( r instanceof SubPage ) {
            SubPage sub = (SubPage) r;
            if( sub.getParent() == parent ) { // don't wrap if the request parent is same as physical parent
                r2 = sub;
            } else {
                r2 = new WrappedSubPage((SubPage) r,(CommonTemplated) parent);
            }
        } else if( r instanceof WrappedSubPage ) {
            r2 = new WrappedSubPage((WrappedSubPage) r,(CommonTemplated) parent);
        } else {
            r2 = r;
        }                               
        return r2;
    }
    
    public static BaseResource get(UUID id) {
        VfsSession vfs = RequestContext.getCurrent().get(VfsSession.class);
        if( vfs == null ) throw new NullPointerException("No VFS session in context");
        NameNode nn = vfs.get(id);
        if( nn == null ) return null;
        return (BaseResource) nn.getData();
    }
}
