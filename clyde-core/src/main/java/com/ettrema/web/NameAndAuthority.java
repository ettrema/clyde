
package com.bradmcevoy.web;

public class NameAndAuthority {
    
    public static NameAndAuthority parse( String user ) {
        if( user == null ) return new NameAndAuthority( null );
        user = stripDomain( user ); // strip any domain that windows might have added
        int pos = user.indexOf( "@" );
        if( pos < 0 ) pos = user.indexOf( "#" );
        if( pos < 0 ) {
            return new NameAndAuthority( user );
        } else {
            String name = user.substring( 0, pos );
            String authority = user.substring( pos + 1, user.length() );
            return new NameAndAuthority( name, authority );
        }
    }

    private static String stripDomain( String user ) {
        int pos = user.indexOf( "\\\\");
        if( pos <= 0 ) return user;
        String s = user.substring( pos+2 );
        return s;
    }

    
    public final String name;
    public final String authority;

    public NameAndAuthority(String name) {
        this(name,null);
    }
    
    public NameAndAuthority(String name, String authority) {
        this.name = name;
        this.authority = authority;
    }

    @Override
    public String toString() {
        if( authority == null ) {
            return name;
        } else {
            return name + "@" + authority;
        }
    }
    
    
}
