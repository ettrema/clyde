package com.bradmcevoy.web.creation;

import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.IUser;
import com.bradmcevoy.web.Page;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.HtmlInput;
import com.bradmcevoy.web.security.CurrentUserService;
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
        ITemplate template = folder.getTemplate( "normal" );

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        StreamUtils.readTo( in, bout );

        Page page;
        if( template == null ) {
            page = new Page( folder, newName );
            HtmlInput root = new HtmlInput( page, "root" );
            root.setDisAllowTemplating( true );
            if( in != null ) {
                root.setValue( bout.toString() );
                page.getComponents().add( root );
            }
        } else {
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
        }
        IUser creator = _( CurrentUserService.class ).getOnBehalfOf();
        if( creator instanceof User ) {
            page.setCreator( (User) creator );
        }
        page.save();
        return page;

    }
}
