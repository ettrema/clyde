package com.bradmcevoy.web;

import com.bradmcevoy.web.component.InitUtils;
import com.ettrema.mail.Attachment;
import com.ettrema.mail.InputStreamConsumer;
import com.ettrema.mail.Utils;
import java.io.InputStream;
import org.jdom.Element;

public class EmailAttachment extends BinaryFile implements Attachment {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( EmailAttachment.class );
    private static final long serialVersionUID = 1L;
    String contentId;
    String disposition;

    public EmailAttachment( ClydeStandardMessage parent, String contentType, String name, String contentId ) {
        super( contentType, parent, name );
        this.contentId = contentId;
    }

    @Override
    public void populateXml( Element e2 ) {
        super.populateXml( e2 );
        InitUtils.setString( e2, "contentId", contentId );
        InitUtils.setString( e2, "disposition", disposition );
    }

    @Override
    public void loadFromXml( Element el ) {
        super.loadFromXml( el );
        contentId = InitUtils.getValue( el, "contentId" );
        disposition = InitUtils.getValue( el, "disposition" );
    }

    @Override
    public void useData( InputStreamConsumer consumer ) {
        InputStream in = null;
        try {
            consumer.execute( in );
        } finally {
            Utils.close( in );
        }
    }

    @Override
    public String getContentId() {
        return contentId;
    }

    @Override
    public String getContentType() {
        return getContentType( null );
    }

    @Override
    public String getDisposition() {
        return disposition;
    }

    @Override
    public int size() {
        Long l = this.getContentLength();
        if( l == null ) return 0;
        return (int) l.longValue();
    }

    @Override
    public boolean is( String type ) {
        boolean b = super.is( type );
        if( b ) return true;
        return type.equals( "attachment" );
    }
}
