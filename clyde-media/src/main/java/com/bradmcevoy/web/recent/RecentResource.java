package com.bradmcevoy.web.recent;

import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.property.BeanPropertyResource;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.BinaryFile;
import com.bradmcevoy.web.File;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.IUser;
import com.bradmcevoy.web.component.InitUtils;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.jdom.Element;

/**
 * Represents a pointer to a recently modified file or folder
 *
 */
@BeanPropertyResource("clyde")
public class RecentResource extends File {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RecentResource.class);
    private static final long serialVersionUID = 1L;
    private String targetHref;
    private String targetName;
    private String updatedByName;
    private UUID updatedById;
    private boolean isBinary;
    private boolean isFolder;

    public RecentResource(Folder parent, BaseResource target, IUser user) {
        super(target.getContentType(), parent, target.getNameNodeId().toString());
        this.targetHref = target.getHref();
        this.targetName = target.getName();
        setUser(user);
        if (target instanceof BinaryFile) {
            isBinary = true;
        }
        if( target instanceof Folder) {
            isFolder = true;
        }
    }

    @Override
    public String getDefaultContentType() {
        return null;
    }




    @Override
    public void _delete() throws ConflictException, BadRequestException, NotAuthorizedException {
        deletePhysically();
    }



    public void setUser(IUser user) {
        if (user != null) {
            this.updatedByName = user.getNameNode().getName();
            this.updatedById = user.getNameNodeId();
        } else {
            this.updatedByName = "Unknown";
        }
    }

    @Override
    protected BaseResource newInstance(Folder parent, String newName) {
        throw new RuntimeException("not supported");
    }

    @Override
    public boolean is(String type) {
        if (type == null) {
            return false;
        }
        if (super.is(type)) {
            return true;
        }
        if (type.equals("recent")) {
            return true;
        }
        String ct = getContentType(null);
        if (ct == null) {
            return false;
        }
        return ct.contains(type);
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
        PrintWriter pw = new PrintWriter(out);
        pw.print("<html><body><h1>The file no longer exists</h1></body></html>");
        pw.flush();
    }

    @Override
    public String checkRedirect(Request request) {
        BaseResource res = getTargetResource();
        if (res != null) {
            return res.getHref();
        } else {
            return null;
        }
    }

    public BaseResource getTargetResource() {
        UUID uuid = UUID.fromString(this.getName());
        NameNode nn = this.vfs().get(uuid);
        if (nn == null) {
            return null;
        } else {
            DataNode dn = nn.getData();
            if (dn == null) {
                return null;
            } else if (dn instanceof BaseResource) {
                BaseResource res = (BaseResource) dn;
                return res;
            } else {
                return null;
            }
        }
    }

    @Override
    protected BaseResource copyInstance(Folder parent, String newName) {
        BaseResource newRes = super.copyInstance(parent, newName);
        // todo
        return newRes;
    }

    @Override
    public void populateXml(Element e2) {
        super.populateXml(e2);
        InitUtils.setString(e2, "targetName", targetName);
        InitUtils.setString(e2, "targetHref", targetHref);
        InitUtils.setString(e2, "updatedByName", updatedByName);
        InitUtils.setString(e2, "updatedById", updatedById.toString());
        InitUtils.set(e2, "isBinary", isBinary);
        InitUtils.set(e2, "isFolder", isFolder);
    }

    public String getTargetHref() {
        return targetHref;
    }

    public String getTargetName() {
        return targetName;
    }

    public UUID getUpdatedById() {
        return updatedById;
    }

    public void setUpdatedById(UUID updatedById) {
        this.updatedById = updatedById;
    }



    public String getUpdatedByName() {
        return updatedByName;
    }

    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }


    public String getThumbHref() {
        if (isBinary) {
            BinaryFile bf = (BinaryFile) getTargetResource();
            return bf.getThumbHref();
        } else if( isFolder ) {
            Folder f = (Folder)getTargetResource();
            return f.getThumbHref();
        } else {
            return "";
        }
    }

    @Override
    public Long getModifiedDateAsLong() {
        Date dt = getModifiedDate();
        if( dt == null ) {
            return null;
        } else {
            return dt.getTime();
        }
    }

    @Override
    public boolean isIndexable() {
        return false;
    }


}
