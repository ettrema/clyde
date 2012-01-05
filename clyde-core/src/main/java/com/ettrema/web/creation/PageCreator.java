package com.ettrema.web.creation;

import com.ettrema.web.security.CurrentUserService;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import com.ettrema.web.ITemplate;
import com.ettrema.web.IUser;
import com.ettrema.web.Page;
import com.ettrema.web.User;
import com.ettrema.web.component.ComponentDef;
import com.ettrema.web.component.ComponentValue;
import com.ettrema.web.security.CurrentUserService;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class PageCreator implements Creator {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( PageCreator.class );

    @Override
    public boolean accepts( String contentType ) {
        if( contentType.contains( "html" ) ) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public BaseResource createResource( Folder folder, String ct, InputStream in, String newName ) throws ReadingException, WritingException {
        // Attempt to find a suitable template
        //ITemplate template = folder.getTemplate( "normal" );
        ITemplate template = folder.getTemplate( "root" );

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        StreamUtils.readTo( in, bout );

        Page page;
        page = (Page) template.createPageFromTemplate( folder, newName );
        ComponentValue cvBody = page.getValues().get( "body" );
        if( cvBody == null ) {
            ComponentDef bodyDef = template.getComponentDef( "body" );
            if( bodyDef != null ) {
                cvBody = bodyDef.createComponentValue( page );
                cvBody.setValue( bout.toString() );
            } else {
                log.warn( "Found template, but no body componentdef. Can't set content!" );
            }
        } else {
            cvBody.setValue( bout.toString() );
        }

        IUser creator = _( CurrentUserService.class ).getOnBehalfOf();
        if( creator instanceof User ) {
            page.setCreator( (User) creator );
        }
        page.save();
        return page;

    }
}
