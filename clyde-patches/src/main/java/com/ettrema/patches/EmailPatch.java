package com.ettrema.patches;

import com.bradmcevoy.context.Context;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsSession;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.EmailDef;
import com.bradmcevoy.web.component.EmailVal;
import com.bradmcevoy.web.console2.PatchApplicator;
import java.util.List;

/**
 *
 * @author brad
 */
public class EmailPatch implements PatchApplicator {

    private static final long serialVersionUID = 1L;
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( EmailPatch.class );

    public String getName() {
        return getClass().getCanonicalName();
    }

    public void setArgs( String[] args ) {
    }

    public void doProcess( Context context ) {
        log.debug( "doProcess" );
        int cnt = 0;
        VfsSession sess = context.get( VfsSession.class );
        List<NameNode> userNodes = sess.find( User.class, null );
        ComponentDef def;
        for( NameNode nn : userNodes ) {
            User user = (User) nn.getData();
            ComponentValue cv = user.getValues().get( "email" );
            if( cv != null ) {
                user.getValues().remove( "email" );
            }
            log.debug( "patch: " + cnt++ + " - " + user.getHref() );
            if( user.getValues().get( "email" ) != null ) {
                throw new RuntimeException( "email not null");
            }
            String email = user.getExternalEmailText();
            user.setExternalEmailTextV2( "default", email );
            ITemplate template = user.getTemplate();
            if( template != null ) {
                def = template.getComponentDef( "email");
                if( def != null && def instanceof EmailDef ) {
                    EmailVal ev = (EmailVal) def.createComponentValue( user );
                    user.getValues().add( ev );
                    ev.setValue( email );
                }
            }
            user.save();
        }
        sess.commit();
        log.debug( "Done!" );
    }

    public void pleaseImplementSerializable() {
    }
}
