package com.ettrema.web;

import com.bradmcevoy.property.BeanPropertyResource;


@BeanPropertyResource("clyde")
public class MusicFile extends BinaryFile {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( MusicFile.class );
    private static final long serialVersionUID = 1L;


    public MusicFile( String contentType, Folder parentFolder, String newName ) {
        super( contentType, parentFolder, newName );
    }

    public MusicFile( Folder parentFolder, String newName ) {
        super( "image", parentFolder, newName );
    }

    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new MusicFile( parent, newName );
    }

    @Override
    public boolean is( String type ) {
        if( "music".equalsIgnoreCase( type ) || "song".equalsIgnoreCase( type ) ) return true;
        return super.is( type );
    }

    @Override
    protected void afterSetContent() {
        super.afterSetContent();
    }
}
