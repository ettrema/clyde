
package com.bradmcevoy.web.component;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.process.ProcessDef;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.ComponentMap;
import com.bradmcevoy.web.Group;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.User;
import java.util.List;
import java.util.Map;
import org.jdom.Element;

public class CreateHostCommand extends CreateWebCommand{
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CreateHostCommand.class);
    private static final long serialVersionUID = 1L; 
    
    protected String domainName;
    protected Text password;
    protected EmailInput email;
    protected String newUserTemplate;
    protected String newGroupTemplate;

    public CreateHostCommand(Addressable container, String name) {
        super(container,name);
        email = new EmailInput( container, "email");
    }

    public CreateHostCommand(Addressable container, Element el) {
        super(container,el);
    }


    public Text getPassword() {
        return this.password;
    }

    public EmailInput getEmail() {
        return email;
    }

    

    @Override
    protected void afterCreate(BaseResource hostRes, RenderContext rc) {
        log.debug( "afterCreate");
        super.afterCreate(hostRes, rc);

        log.debug( "add new group");
        Group group = hostRes.getHost().createGroup("administrators", newGroupTemplate);

        String pwd = "";
        if( getPassword() != null ) {
            pwd = getPassword().getValue();
        }
        User user = hostRes.getHost().createUser("admin", pwd, group, newUserTemplate);
        if( email != null ) {
            String sEmail = email.getFormattedValue();
            log.debug( "setting new user's email: " + sEmail);
            user.setExternalEmailText( sEmail );
            Object o = user.getParams().get( "email");
        }
        user.save();
        log.debug( "created user: " + user);

        hostRes.save();
        if( ProcessDef.scan(hostRes) ) {
            hostRes.save();
        }
    }

    @Override
    public String getNameToCreate(RenderContext rc) {
        String s = super.getNameToCreate(rc);
        if( s.contains(".") ) return s;
        return s + "." + domainName;
    }

    

    @Override
    public void fromXml( Element el ) {
        super.fromXml( el );
        this.domainName = InitUtils.getValue( el, "domainName");
        this.newUserTemplate = InitUtils.getValue( el, "newUserTemplate");
        this.newGroupTemplate = InitUtils.getValue( el, "newGroupTemplate");
    }
    
    @Override
    protected void fromXml(ComponentMap map) {
        super.fromXml(map);
        Component c;
        c = map.get("password");
        if( c == null ) {
            password = new Text(this, "password");
        } else {
            password = (Text) c;
        }
        c = map.get("email");
        if( c == null ) {
            email = new EmailInput(this, "email");
        } else {
            email = (EmailInput) c;
        }
    }

    @Override
    public void populateXml( Element e2 ) {
        super.populateXml( e2 );
        InitUtils.setString( e2, "domainName", domainName);
        InitUtils.setString( e2, "newUserTemplate", newUserTemplate);
        InitUtils.setString( e2, "newGroupTemplate", newGroupTemplate);

        password.toXml(container, e2);
        if( email != null ) {
            email.toXml( container, e2 );
        }
    }

    @Override
    public void onPreProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        super.onPreProcess(rc, parameters, files);
        password.onPreProcess(rc, parameters, files);
        if( email != null ) {
            email.onPreProcess( rc, parameters, files );
        }
    }

    @Override
    public boolean validate(RenderContext rc) {
        boolean b = super.validate(rc);
        b = b & password.validate(rc);
        b = b & email.validate(rc);
        return b;
    }

    @Override
    protected boolean validateName(RenderContext rc) {
        boolean b = super.validateName(rc);
        if( b ) {
            String n = getNameToCreate(rc);
            log.debug( "validate: " + n);
            if( n != null && n.length() > 0 ) {
                List<NameNode> existing = vfs().find(Host.class, n);
                if( existing != null && existing.size() > 0 ) {
                    newName.setValidationMessage("That host name is already taken. Please choose another");
                    log.warn("Existing hostxx, but not in this folder: " + existing.get(0).getId() + " - " + existing.get(0).getParent().getName() +"/" + existing.get(0).getName() );
                    return false;
                }
            } else {
                b = false;
                log.warn("create name is empty or null");
                newName.setValidationMessage( "Please enter a name");
            }
        }
        return b;
    }
    
    
}
