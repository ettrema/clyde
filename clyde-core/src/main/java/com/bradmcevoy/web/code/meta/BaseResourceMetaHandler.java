package com.bradmcevoy.web.code.meta;

import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.BaseResource.RoleAndGroup;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.web.groups.GroupService;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
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
        log.warn( "populateXml" );
        InitUtils.setString( e2, "redirect", res.getRedirect() );
        List<RoleAndGroup> groupPermissions = res.getGroupPermissions();
        if( groupPermissions != null && !groupPermissions.isEmpty() ) {
            Element elGroups = new Element( "groups", CodeMeta.NS );
            e2.addContent( elGroups );
            log.trace( "add groups" );
            for( RoleAndGroup rag : res.getGroupPermissions() ) {
                Element elRag = new Element( "group", CodeMeta.NS );
                elGroups.addContent( elRag );
                elRag.setAttribute( "group", rag.getGroupName() );
                elRag.setAttribute( "role", rag.getRole().name() );
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
        updateFromXml( res, el, false);
    }

    void updateFromXml( BaseResource res, Element el, boolean includeContentVals ) {
        commonTemplatedMetaHandler.updateFromXml( res, el, includeContentVals );
        
        res.setRedirect( InitUtils.getValue( el, "redirect" ) );


        Element elGroups = el.getChild( "groups" );
        if( elGroups != null ) {
            log.warn( "processing groups" );
            GroupService groupService = _( GroupService.class );
            for( Object oGroup : elGroups.getChildren() ) {
                Element elGroup = (Element) oGroup;
                String groupName = elGroup.getAttributeValue( "group" );
                UserGroup group = groupService.getGroup( res, groupName );
                if( group != null ) {
                    String roleName = elGroup.getAttributeValue( "role" );
                    if( !StringUtils.isEmpty( roleName ) ) {
                        roleName = roleName.trim();
                        try {
                            Role role = Role.valueOf( roleName );
                            res.permissions( true ).grant( role, group );
                        } catch( Exception e ) {
                            log.error( "unknown role: " + roleName, e );
                        }
                    } else {
                        log.warn( "empty role name" );
                    }
                } else {
                    log.warn( "group not found: " + groupName );
                }
            }
        } else {
            log.warn( "no groups element" );
        }

    }
}
