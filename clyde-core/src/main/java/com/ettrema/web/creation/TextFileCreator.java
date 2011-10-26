package com.ettrema.web.creation;

import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import com.ettrema.web.IUser;
import com.ettrema.web.TextFile;
import com.ettrema.web.User;
import com.ettrema.web.security.CurrentUserService;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class TextFileCreator implements Creator {

    @Override
    public boolean accepts( String ct ) {
        return ct.contains( "text" ) || ct.contains( "javascript" );
    }

    @Override
    public BaseResource createResource( Folder folder, String ct, InputStream in, String newName ) throws ReadingException, WritingException {
        TextFile tf = new TextFile( ct, folder, newName );
        IUser creator = _(CurrentUserService.class).getOnBehalfOf();
        if( creator instanceof User){
            tf.setCreator( (User)creator );
        }

        tf.save();
        if( in != null ) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            StreamUtils.readTo( in, bout );
            tf.setContent( bout.toString() );
            tf.save();
        }
        return tf;
    }
}
