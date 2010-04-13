package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.web.component.InitUtils;
import com.ettrema.vfs.aws.BucketOwner;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.jdom.Element;

public class Host extends Web implements BucketOwner {
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Host.class);
    
    private static final long serialVersionUID = 1L;
    
    Path hostPath;
    private boolean disabled;
    
    public Host(Folder parent,String name) {
        super(parent,name);
        this.getTemplate();
    }

    @Override
    protected BaseResource copyInstance(Folder parent, String newName) {
        Host hNew = (Host) super.copyInstance(parent, newName);
        hNew.hostPath = this.hostPath;
        return hNew;
    }

    @Override
    public boolean is( String type ) {
        if( type.equalsIgnoreCase( "host")) return true;
        return super.is( type );
    }


    
    
    @Override
    public List<Template> getAllowedTemplates() {
        if( templateSpecs == null ) {
            return TemplateSpecs.findApplicable(this);
        } else {
            return templateSpecs.findAllowed(this);
        }
    }

    /**Sets the path to the host that this will be an alias for. Does not save
     * 
     * @param pAliasPath
     */
    public void setAliasPath(Path pAliasPath) {
        this.hostPath = pAliasPath;
    }
    
    @Override
    protected BaseResource newInstance(Folder parent, String newName) {
        return new Host(parent, newName);
    }
    
    
    @Override
    public void loadFromXml(Element el) {
        super.loadFromXml(el);
        String s = el.getAttributeValue("hostPath");
        if( s != null && s.trim().length()>0 ) {
            hostPath = Path.path(s.trim());
        }
        this.disabled = InitUtils.getBoolean( el, "disabled");
    }

    

    @Override
    public void populateXml( Element e2 ) {
        super.populateXml( e2 );
        if( hostPath != null ) {
            e2.setAttribute("hostPath", hostPath.toString());
        }
        InitUtils.setBoolean( e2, "disabled", disabled);
    }


    
    
    
    public Host getAliasedHost() {
        if( hostPath == null ) return null;
        NameNode nn = vfs().find(hostPath);
        if( nn == null ) {
            log.warn("Did not fina alised host name node: " + hostPath);
            return null;
        }
        Resource res = (Resource) nn.getData();
        if( res == null ) {
            log.warn("Did not find aliased host data node: " + hostPath + " in namenode: " + nn.getId());
            return null;
        } else if( res instanceof Host ) {
            return (Host) res;
        } else {
            log.warn("object is not a host: " + hostPath + ".is a:" + res.getClass().getName());
            return null;
        }
    }

    @Override
    public Resource child(String name) {
        if( hostPath != null ) {
            Host host = getAliasedHost();
            if( host != null ) {
                return host.child(name);
            }
        }
        return super.child(name);
    }

    @Override
    public List<Templatable> getChildren() {
        if( hostPath != null ) {
            Host host = getAliasedHost();
            if( host != null ) {
                return host.getChildren();
            }
        }
        return super.getChildren();
    }

    @Override
    public CollectionResource createCollection( String newName, boolean commit ) throws ConflictException{
        if( hostPath != null ) {
            Host host = getAliasedHost();
            if( host != null ) {
                return host.createCollection(newName, commit );
            }
        }
        return super.createCollection( newName, commit );
    }


    @Override
    public Resource createNew( String newName, InputStream in, Long length, String contentType ) throws IOException, ConflictException {
        if( hostPath != null ) {
            Host host = getAliasedHost();
            if( host != null ) {
                return host.createNew( newName, in, length, contentType );
            }
        }
        return super.createNew( newName, in, length, contentType );
    }




    @Override
    public String getUrl() {
        return "/";
    }


    boolean isAlias() {
        return (hostPath != null );
    }
    
    @Override
    public Host getHost() {
        return this;
    }

    
    public Folder getUsers() {
        return getUsers(false);
    }
    
    public Folder getUsers(boolean create) {
        Folder users = (Folder) this.child("users");
        if( users == null ) {
            if( create ) {
                try {
                    users = (Folder) this.createCollection("users", false);
                    users.templateSpecs.add( "+user" );
                    users.templateSpecs.add( "+group" );
                    users.templateSpecs.add( "-*" );
                } catch( ConflictException ex ) {
                    throw new RuntimeException( ex );
                }
            }
        }
        return users;

    }

    public User findUser(String name) {
        Folder users = getUsers();
        if( users == null ) return null;
        Resource res = users.child(name);
        if( res != null ) {
            if( res instanceof User ) {
                return (User) res;
            } else {
                log.warn("found an instance, but not a user: " + res.getName());
            }
        }
        return null;

    }

    /**
     * Authenticates against this particular domain (non recursive)
     * 
     * 
     * @param user
     * @param password
     * @return - null if not authenticated. otherwise, the user object
     */
    public User doAuthenticate(String user, String password) {
        User userRes = findUser( user );

        if( userRes == null ) {
            // log.debug("user not found: " + user + " in domain: " + getName());
            return null;
        }
        
        // have found a user of that name, so authenticate it
        boolean b = userRes.checkPassword(password);
        if( b ) {
            return userRes;
        } else {
            return null;
        }
    }

    public User doAuthenticate( String user, DigestResponse digestRequest ) {
        User userRes = findUser( user );

        if( userRes == null ) {
            log.debug("user not found: " + user + " in domain: " + getName());
            return null;
        }

        // have found a user of that name, so authenticate it
        boolean b = userRes.checkPassword(digestRequest);
        if( b ) {
            return userRes;
        } else {
            log.warn( "checkPassword failed in host: " + this.getName());
            return null;
        }
    }


    @Override
    protected void afterSave() {
        super.afterSave();
        Folder users = getUsers(true);
        users.save();        
    }

    public Group findGroup(String groupName) {
        Resource r = getUsers().child(groupName);
        if( r instanceof Group ) {
            Group g = (Group) r;
            return g;
        } else {
            return null;
        }               
    }

    public User createUser( String name, String pwd) {
        return createUser( name, pwd, null, null );
    }

    /**
     * Create and save a user. Will create the user from the templateName
     * if given and it can be found
     *
     * @param name
     * @param pwd
     * @param templateName
     * @return
     */
    public User createUser( String name, String pwd, Group group, String templateName ) {
        Folder users = getUsers( true );
        if( users.childExists( name )) {
            throw new RuntimeException( "Resource already exists: " + name);
        }
        ITemplate template = null;
        if( templateName != null && templateName.length()>0 ) {
            template = users.getTemplate( templateName );
        }
        User user;
        if( template == null ) {
            user = new User( users, name);
        } else {
            BaseResource newRes = template.createPageFromTemplate( users, name );
            if( newRes instanceof User ) {
                user = (User)newRes;
            } else {
                throw new RuntimeException( "Got a resource which is not a user. Template: " + template);
            }
        }
        user.password.setValue( pwd );
        if( group != null ) {
            user.getGroupNames().add( group.getName());
        }
        user.save();
        return user;

    }

    public Group createGroup( String name, String templateName ) {
        Folder users = getUsers( true );
        ITemplate template = null;
        if( templateName != null && templateName.length()>0 ) {
            template = users.getTemplate( templateName );
        }
        if( template == null ) {
            Group group = new Group( users, name);
            group.save();
            return group;
        } else {
            BaseResource newRes = template.createPageFromTemplate( users, name );
            newRes.save();
            if( newRes instanceof Group ) {
                return (Group)newRes;
            } else {
                throw new RuntimeException( "Got a resource which is not a Group. Template: " + template);
            }
        }
    }
    
    public void disable() {
        log.debug( "disable");
        this.disabled = true;
    }

    public void enable() {
        log.debug( "enable");
        this.disabled = false;
    }

    public boolean isDisabled() {
        return disabled;
    }

}
