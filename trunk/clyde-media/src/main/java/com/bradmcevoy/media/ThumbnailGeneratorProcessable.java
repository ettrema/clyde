package com.bradmcevoy.media;

import com.ettrema.context.Context;
import com.ettrema.grid.Processable;
import java.io.Serializable;
import java.util.UUID;

public class ThumbnailGeneratorProcessable implements Processable, Serializable {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ThumbnailGeneratorProcessable.class );
    private static final long serialVersionUID = 1L;
    final String targetName;
    final UUID imageFileNameNodeId;

    public ThumbnailGeneratorProcessable( UUID imageFileNameNodeId, String name ) {
        this.targetName = name;
        this.imageFileNameNodeId = imageFileNameNodeId;
    }

    public void doProcess( Context context ) {
        context.get( ThumbGeneratorService.class).initiateGeneration(context, targetName, imageFileNameNodeId );
    }

    public void pleaseImplementSerializable() {
    }
}
