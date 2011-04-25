package com.bradmcevoy.web.component;

import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Templatable;
import org.jdom.Element;

public class EmailVal extends ComponentValue {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( EmailVal.class );
    private static final long serialVersionUID = 1L;

    public EmailVal( String name, Addressable container ) {
        super( name, container );
    }

    public EmailVal( Element el, Templatable container ) {
        super( el, container );
    }

   
    @Override
    public String getValue() {
        System.out.println("email: getValue");
        Addressable p = getContainer();
        if( p == null ) {
            throw new RuntimeException( "No container, so can't get email" );
        } else if( p instanceof BaseResource ) {
            BaseResource res = (BaseResource) p;
            String email = res.getExternalEmailTextV2( "default" );
            return email;
        } else {
            throw new RuntimeException( "Parent is not a BaseResource, so cant access email field" );
        }
    }

    @Override
    public void setValue( Object value ) {
        if( value == null || value instanceof String ) {
            String email = (String) value;
            Addressable p = getContainer();
            if( p == null ) {
                throw new RuntimeException( "No container, so can't get email" );
            } else if( p instanceof BaseResource ) {
                BaseResource res = (BaseResource) p;
                res.setExternalEmailTextV2( "default", email );
            } else {
                throw new RuntimeException( "Parent is not a BaseResource, so cant access email field" );
            }
        } else {
            throw new RuntimeException( "Supplied value wasnt a string. Is a: " + value.getClass());
        }
    }
}
