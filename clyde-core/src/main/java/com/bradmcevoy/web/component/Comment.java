
package com.bradmcevoy.web.component;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.vfs.DataNode;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.web.CommonTemplated;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Comment implements DeletableResource, PropFindableResource, DataNode, Serializable{
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Comment.class);
    private static final long serialVersionUID = 1L;
    
    private static final DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.ENGLISH);

    private transient NameNode nameNode;

    private UUID dataNodeId;
    public java.util.Date created;
    public String text;
    public String user;
    private String name;

    public Comment() {
    }

    public Comment(Date created, String text, String user) {
        this.created = created;
        this.text = text;
        this.user = user;
        this.name = "comment_" + created.getTime();
    }
        
    public Date getCreated() {
        return created;
    }

    public String getText() {
        return text;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public void delete() {
        this.nameNode.delete();
    }

    @Override
    public String getUniqueId() {
        return nameNode.getId().toString();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object authenticate(String user, String password) {
        return getParent().authenticate(user, password);
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {
        return getParent().authorise(request, method, auth);
    }

    @Override
    public String getRealm() {
        return getParent().getRealm();
    }

    @Override
    public Date getModifiedDate() {
        return created;
    }

    @Override
    public String checkRedirect(Request arg0) {
        return null;
    }

    @Override
    public Date getCreateDate() {
        return created;
    }

    @Override
    public void setId(UUID id) {
        this.dataNodeId = id;
    }

    @Override
    public UUID getId() {
        return this.dataNodeId;
    }

    @Override
    public void init(NameNode nameNode) {
        this.nameNode = nameNode;
    }

    @Override
    public void onDeleted(NameNode nameNode) {
        
    }

    CommonTemplated getParent() {
        return (CommonTemplated) this.nameNode.getParent().getData();
    }

    void save() {
        this.nameNode.save();
    }

}
