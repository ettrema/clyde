package com.ettrema.patches;

import com.ettrema.web.Folder;
import com.ettrema.web.ITemplate;
import com.ettrema.web.User;
import com.ettrema.web.component.ComponentDef;
import com.ettrema.web.component.ComponentValue;
import com.ettrema.web.component.EmailDef;
import com.ettrema.web.component.EmailVal;
import com.ettrema.web.console2.PatchApplicator;
import com.ettrema.context.Context;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
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
            log.debug( "patch: " + cnt++ + " - " + user.getHref() );
            String email = user.getExternalEmailText();
            log.debug( "email1: " + email);

            log.debug( "listing cvs");
            for( ComponentValue cv : user.getValues().values()) {
                log.debug( "cv: " + cv.getName() + " = " + cv.getValue());
            }

            ComponentValue cv = user.getValues().get( "email" );
            log.debug( "cv : " + cv);
            if( cv != null && cv.getValue() != null ) {
                log.debug( "cv.val: " + cv.getValue());
                Object oEmail = cv.getValue();
                if( oEmail instanceof String) {
                    log.debug( " is string");
                    email = (String) oEmail;
                }
                user.getValues().remove( "email" );
            }
            log.debug( " email2: " + email);
            if( email != null )  {
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
        }
        sess.commit();
        log.debug( "Done!" );
    }

    public void pleaseImplementSerializable() {
    }

    public void setCurrentFolder( Folder currentResource ) {

    }

}
