package com.ettrema.web;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.logging.LogUtils;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.Relationship;
import com.ettrema.web.children.ChildFinder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ettrema.context.RequestContext.*;

/**
 * Implements a collectionresource whose children is that of another linked folder
 *
 * The linked folder is determined by the linkedto property
 *
 * @author brad
 */
public class LinkedFolder extends BaseResource implements CollectionResource, GetableResource, PropFindableResource, XmlPersistableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LinkedFolder.class);
    private static final long serialVersionUID = 1L;
    public static String REL_LINKED_TO = "_sys_linked_to";

    /**
     * Get all the linked folders that are links to this folder
     * 
     * @param from
     * @return 
     */
    public static List<LinkedFolder> getLinkedDestinations(Folder from) {
        List<Relationship> rels = from.getNameNode().findFromRelations(REL_LINKED_TO);
        if (rels == null || rels.isEmpty()) {
            return null;
        } else {
            List<LinkedFolder> list = new ArrayList<LinkedFolder>();
            for (Relationship rel : rels) {
                NameNode nFrom = rel.from();
                if (nFrom == null) {
                    log.warn("from node does not exist");
                    return null;
                } else {
                    DataNode dnFrom = nFrom.getData();
                    if (dnFrom == null) {
                        log.warn("to node has no data");
                    } else {
                        if (dnFrom instanceof LinkedFolder) {
                            LinkedFolder cr = (LinkedFolder) dnFrom;
                            list.add(cr);
                        } else {
                            log.warn("from node is not a: " + LinkedFolder.class + " is a: " + dnFrom.getClass());
                        }
                    }
                }
            }
            return list;
        }
    }

    public LinkedFolder(Folder parentFolder, String newName) {
        super(null, parentFolder, newName);
    }

    @Override
    public boolean is(String type) {
        if (super.is(type)) {
            return true;
        } else {
            return type.equals("folder") || type.equals("link");
        }
    }

    @Override
    public String getDefaultContentType() {
        return null;
    }

    @Override
    protected BaseResource newInstance(Folder parent, String newName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isIndexable() {
        return false;
    }

    @Override
    public void onDeleted(NameNode nameNode) {
    }

    @Override
    public Resource child(String childName) {
        Folder linkedTo = getLinkedTo();
        if (linkedTo != null) {
            LogUtils.trace(log, "child: delegate to childFinder with linkedto folder", childName);
            return _(ChildFinder.class).find(childName, linkedTo);
        } else {
            LogUtils.trace(log, "child: did not find linkedto folder", childName);
            return null;
        }
    }

    public void setLinkedTo(Folder cr) {
        List<Relationship> rels = this.getNameNode().findToRelations(REL_LINKED_TO);

        // delete previous relations
        if (rels != null && rels.size() > 0) {
            for (Relationship rel : rels) {
                rel.delete();
            }
        }
        // create new relation
        Relationship newRel = this.getNameNode().makeRelation(cr.getNameNode(), REL_LINKED_TO);
        cr.getNameNode().onNewRelationship(newRel);
        LogUtils.trace(log, "setLinkedTo: created relationship to", cr.getName());
        cr.setLinkedFolders(true);
        cr.save();
    }

    public Folder getLinkedTo() {
        //List<Relationship> rels = this.getNameNode().findToRelations(REL_LINKED_TO);
        List<Relationship> rels = this.getNameNode().findFromRelations(REL_LINKED_TO);
        if (rels == null || rels.isEmpty()) {
            log.trace("getLinkedto: No relationships");
            return null;
        } else {
            if (rels.size() > 1) {
                log.warn("multiple relations found, using first only");
            }
            Relationship rel = rels.get(0);
            NameNode nTo = rel.to();
            if (nTo == null) {
                log.warn("to node does not exist");
                return null;
            } else {
                DataNode dnTo = nTo.getData();
                if (dnTo == null) {
                    log.warn("to node has no data");
                    return null;
                } else {
                    if (dnTo instanceof Folder) {
                        Folder cr = (Folder) dnTo;
                        log.trace("Found linked to folder");
                        return cr;
                    } else {
                        log.warn("to node is not a: " + Folder.class + " is a: " + dnTo.getClass());
                        return null;
                    }
                }
            }
        }
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException{
        CollectionResource cr = getLinkedTo();
        if (cr == null) {
            log.trace("getChildren: no linked resource");
            return Collections.EMPTY_LIST;
        } else {
            log.trace("getChildren: delegate to linked resource");
            return cr.getChildren();
        }
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {
        log.trace("authorise: " + auth);
        boolean result;
        // Certain methods delegate to the wrapped collection, eg PROPFIND. While others apply to this link eg DELETE
        if (method.equals(Method.DELETE)) {
            log.trace("is a delete, so authorise against this resource");
            result = super.authorise(request, method, auth);
        } else {
            Folder linkedTo = getLinkedTo();
            if (linkedTo == null) {
                log.trace("couldnt find linkedTo, so authorise against this link");
                result = super.authorise(request, method, auth);
            } else {
                log.trace("forward auth request to target");
                result = linkedTo.authorise(request, method, auth);
            }
        }
        log.trace("authorise: " + auth + " -> " + result);
        return result;
    }
}
