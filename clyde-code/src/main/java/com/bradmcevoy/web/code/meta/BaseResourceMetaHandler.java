package com.bradmcevoy.web.code.meta;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.security.Subject;
import com.bradmcevoy.utils.JDomUtils;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.BaseResource.RoleAndGroup;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.web.groups.GroupService;
import com.bradmcevoy.web.security.Permission;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import com.bradmcevoy.web.security.Permissions;
import com.bradmcevoy.web.security.UserGroup;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class BaseResourceMetaHandler {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( BaseResourceMetaHandler.class );
    private final CommonTemplatedMetaHandler commonTemplatedMetaHandler;

    public BaseResourceMetaHandler( CommonTemplatedMetaHandler commonTemplatedMetaHandler ) {
        this.commonTemplatedMetaHandler = commonTemplatedMetaHandler;
    }

    public void populateXml( Element e2, BaseResource res ) {
        log.trace( "populateXml" );
        InitUtils.setString( e2, "redirect", res.getRedirect() );
        Element elPerms = null;
        List<RoleAndGroup> groupPermissions = res.getGroupPermissions();
        if( groupPermissions != null && !groupPermissions.isEmpty() ) {
            log.trace( "add groups" );
            for( RoleAndGroup rag : res.getGroupPermissions() ) {
                Element elRag = new Element( "groupPerm", CodeMeta.NS );
                if( elPerms == null ) {
                    elPerms = new Element( "permissions", CodeMeta.NS );
                    e2.addContent( elPerms );
                }
                elPerms.addContent( elRag );
                elRag.setAttribute( "group", rag.getGroupName() );
                elRag.setAttribute( "role", rag.getRole().name() );
            }
        }
        Permissions perms = res.permissions();
        if( perms != null ) {
            for( Permission perm : perms ) {
                if( elPerms == null ) {
                    elPerms = new Element( "permissions", CodeMeta.NS );
                    e2.addContent( elPerms );
                }

                Element elRag;
                Subject grantee = perm.getGrantee();
                if( grantee instanceof User ) {
                    elRag = new Element( "userPerm", CodeMeta.NS );
                    User granteeUser = (User) grantee;
                    elRag.setAttribute( "path", granteeUser.getUrl() );
                } else if( grantee instanceof UserGroup ) {
                    UserGroup granteeGroup = (UserGroup) grantee;
                    elRag = new Element( "groupPerm", CodeMeta.NS );
                    elRag.setAttribute( "name", granteeGroup.getSubjectName() );
                } else {
                    log.debug( "unsupported permission recipient type: " + grantee.getClass() );
                    elRag = null;
                }
                if( elRag != null ) {
                    elPerms.addContent( elRag );
                    elRag.setAttribute( "role", perm.getRole().toString() );
                }
            }
        }

//        List<Relationship> rels = res.getNameNode().findFromRelations( null );
//        if( !CollectionUtils.isEmpty( rels ) ) {
//            Element elRels = new Element( "relations", CodeMeta.NS );
//            e2.addContent( elRels );
//            Element elFrom = new Element( "from", CodeMeta.NS );
//            elRels.addContent( elFrom );
//            for( Relationship r : rels ) {
//                Element elRel = new Element( "relationship", CodeMeta.NS );
//                elFrom.addContent( elRel );
//                elRel.setAttribute( "relationship", r.relationship() );
//                NameNode nTo = r.to();
//                if( nTo != null ) {
//                    elRel.setAttribute( "id", nTo.getId().toString() );
//                    elRel.setAttribute( "name", nTo.getName() );
//                }
//            }
//
//
//            Element elTo = new Element( "to", CodeMeta.NS );
//            elRels.addContent( elTo );
//            for( Relationship r : res.getNameNode().findToRelations( null ) ) {
//                Element elRel = new Element( "relationship", CodeMeta.NS );
//                elFrom.addContent( elRel );
//                elRel.setAttribute( "relationship", r.relationship() );
//                NameNode nFrom = r.from();
//                if( nFrom != null ) {
//                    elRel.setAttribute( "id", nFrom.getId().toString() );
//                    elRel.setAttribute( "name", nFrom.getName() );
//                }
//            }
//        }
        commonTemplatedMetaHandler.populateXml( e2, res, false ); // do not include content fields (title,body) because they will be in the content file
    }

    void updateFromXml( BaseResource res, Element el ) {
        updateFromXml( res, el, false );
    }

    void updateFromXml( BaseResource res, Element el, boolean includeContentVals ) {

        log.trace( "updateFromXml2" );

        commonTemplatedMetaHandler.updateFromXml( res, el, includeContentVals );

        res.setRedirect( InitUtils.getValue( el, "redirect" ) );


        List<Element> permElements = JDomUtils.childrenOf( el, "permissions", CodeMeta.NS );
        log.trace( "processing permissions: " + permElements.size() );
        GroupService groupService = _( GroupService.class );
        for( Element elPerm : permElements ) {
            String roleName = elPerm.getAttributeValue( "role" );
            Role role;
            if( !StringUtils.isEmpty( roleName ) ) {
                roleName = roleName.trim();
                try {
                    role = Role.valueOf( roleName );
                } catch( Exception e ) {
                    log.error( "unknown role: " + roleName, e );
                    throw new RuntimeException( "Unknown role: " + roleName);
                }
                String type = elPerm.getName();
                if( type.equals( "groupPerm" ) ) {
                    String groupName = elPerm.getAttributeValue( "group" );
                    if( StringUtils.isEmpty( groupName) ) {
                        throw new RuntimeException( "Group attribute is empty");
                    }
                    UserGroup group = groupService.getGroup( res, groupName );
                    if( group != null ) {
                        res.permissions( true ).grant( role, group );
                    } else {
                        throw new RuntimeException( "Group not found: " + groupName);
                    }
                } else if( type.equals( "userPerm" ) ) {
                    String userPath = elPerm.getAttributeValue( "path" );
                    Resource r = res.getHost().find( userPath );
                    if( r == null ) {
                        throw new RuntimeException( "User path not found: " + userPath + " in host: " + res.getHost().getName() );
                    } else if( r instanceof User ) {
                        User u = (User) r;
                        res.permissions( true ).grant( role, u );
                    }
                } else {
                    throw new RuntimeException( "Unknown permission type: " + type);
                }
            } else {
                throw new RuntimeException( "empty role name" );
            }

        }
    }
}
