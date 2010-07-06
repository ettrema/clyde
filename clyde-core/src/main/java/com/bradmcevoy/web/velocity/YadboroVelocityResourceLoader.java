package com.bradmcevoy.web.velocity;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.vfs.DataNode;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsUtils;
import com.bradmcevoy.web.Page;
import com.bradmcevoy.web.component.ComponentValue;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;

public class YadboroVelocityResourceLoader extends org.apache.velocity.runtime.resource.loader.ResourceLoader {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(YadboroVelocityResourceLoader.class);
    
    static final ThreadLocal<String> tlCurrentTemplate = new ThreadLocal<String>();
    
    static void setCurrentTemplate(String sTemplate) {
        tlCurrentTemplate.set(sTemplate);
    }

    @Override
    public void init(ExtendedProperties props) {
    }

    @Override
    public InputStream getResourceStream(String name) throws ResourceNotFoundException {
//        log.debug("getResourceAsStream: " + name);
        if( name.equals(".")) {
            String s = tlCurrentTemplate.get();
            return new ByteArrayInputStream(s.getBytes());
        }

        Path p = Path.path(name);
        ComponentValue cv = getComponentValue(p);
        if (cv == null) {
            log.trace("found page path but not component. " + p);
            return null;
        } else {
            Object o = cv.getValue();
            if (o == null) {
                log.warn("found component, but value is null");
                return null;
            } else {
                String s = o.toString();
                return new ByteArrayInputStream(s.getBytes());
            }
        }
    }

    public ComponentValue getComponentValue(Path p) {        
        Page page = getResourcePage(p);
        if (page == null) {
            return null;
        }
        ComponentValue cv = page.getValues().get(p.getName());
        if (cv == null) {
            log.warn("found page path but not component. " + p);
            return null;
        } else {
            return cv;
        }

    }

    public Page getResourcePage(Path p) throws ResourceNotFoundException {
        Path pagePath = p.getParent();
        if( pagePath == null ) return null;
        NameNode nn = VfsUtils.find(pagePath);
        if (nn == null) {
            log.warn("not found: " + pagePath);
            return null;
        }
        DataNode dn = nn.getData();
        if (dn == null) {
            log.warn("found, but datanode is nul: " + pagePath);
            return null;
        }
        if (dn instanceof Page) {
            Page page = (Page) dn;
            return page;
        } else {
            log.warn("found, nut is not a pge: " + dn.getClass() + " - " + pagePath);
            return null;
        }
    }

    @Override
    public boolean isSourceModified(Resource res) {
//        log.debug("isResourceModified: " + res);
        return true;
//        if( res.getName().equals(".")) {
//            return true;
//        }
//        long t = res.getLastModified();
//        long tNow = getLastModified(res);
//        return (t != tNow);
    }

    @Override
    public long getLastModified(Resource res) {
//        log.debug("getLastModified: " + res);
        return 0;
//        if( res.getName().equals(".")) {
//            return (new Date()).getTime();
//        }
//        Path p = Path.path(res.getName());
//        Page page = getResourcePage(p);
//        if( page == null ) {
//            return (new Date()).getTime();
//        } else {
//            Date dt = page.getModifiedDate();
//            if( dt == null ) {
//                return (new Date()).getTime();
//            } else {
//                return dt.getTime();
//            }
//        }
    }
}
