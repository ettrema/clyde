
package com.bradmcevoy.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.utils.StringUtils;
import com.bradmcevoy.web.Group;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.User;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.jdom.Element;

public class GroupsDef extends TextDef {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GroupsDef.class);
    private static final long serialVersionUID = 1L;
    
    public GroupsDef(Addressable container, String name) {
        super(container,name);
    }

    public GroupsDef(Addressable container, Element el) {
        super(container,el);
    }

    @Override
    public void onPreProcess(ComponentValue componentValue, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        Path compPath = getPath(rc);
        String key = compPath.toString();
        if( !parameters.containsKey(key) ) {
            return ;
        }
        String s = parameters.get(key);        
        List<String> names = parseNames(s);

        User u = parent(componentValue,rc);
        List<String> oldNames = parseNames((String) componentValue.getValue());
        rc.addAttribute("old.group.names", oldNames);
        componentValue.setValue(StringUtils.toString(names));
        u.setGroupNames( names );
    }

    User parent(ComponentValue componentValue, RenderContext rc) {
        Addressable parent = componentValue.getContainer();
        if( parent == null ) throw new NullPointerException("Parent is not set on the componentvalue");
        if( parent instanceof User ) {
            User u = (User) parent;
            return u;
        } else {
            throw new RuntimeException("Not a user: " + parent.getClass());
        }        
    }
    
    public List<String> parseNames(String s) {
        return StringUtils.fromString(s);
    }

    @Override
    public void changedValue(ComponentValue cv) {
        log.debug( "changedValue: " + cv.getValue());
        String s = (String) cv.getValue();
        List<String> names = parseNames(s);
        Addressable parent = cv.getContainer();
        if( parent == null ) throw new NullPointerException("Parent is not set on the componentvalue");
        if( parent instanceof User ) {
            User u = (User) parent;
            u.setGroupNames(names);
        } else {
            throw new RuntimeException("Not a user: " + parent.getClass());
        }
        super.changedValue(cv);
    }

    @Override
    public boolean validate(ComponentValue c, RenderContext rc) {
        User u = parent(c,rc);
        
        List<String> oldNames = (List<String>) rc.get("old.group.names");
        String sNames = (String) c.getValue();        
        List<String> newNames = parseNames(sNames);
        for( String n : newNames ) {
            Group g = findGroup(n, u);
            if( g == null) {
                c.setValidationMessage("Group does not exist: " + n);
                return false;
            }
        }

       
        Collection<String> newlyAddedNames = CollectionUtils.subtract(newNames, oldNames);
        for( String n : newlyAddedNames ) {
            Group g = findGroup(n, u);
            if( g.isSecure() ) {
                Auth auth = rc.getAuth();
                if( auth == null ) {
                    c.setValidationMessage("Must be logged in to add secure group: " + n);
                    return false;
                }
                User currentUser = (User) auth.getTag();
                if( !currentUser.isInGroup(g) && !currentUser.owns(u.getWeb()) ) {
                    c.setValidationMessage("You must be a member of the secure group (or owner of this web) to assign the group: " + g.getName());
                    return false;
                }
            }
        }
        return true;
    }

    private Group findGroup(String n, User u) {
        Host host = u.getHost();
        if( host == null ) throw new RuntimeException("no host");
        Group g = host.findGroup(n);
        return g;
    }
    
    
}
