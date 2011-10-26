package versioning;

import com.ettrema.event.Event;
import com.ettrema.event.EventListener;
import com.ettrema.web.BaseResource;
import com.ettrema.web.ComponentValueMap;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.RelationalNameNode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class VersionManager implements EventListener {

    private final List<VersioningHandler> versioningHandlers;

    public VersionManager( List<VersioningHandler> versioningHandlers ) {
        this.versioningHandlers = versioningHandlers;
    }

    public VersionManager() {
        this.versioningHandlers = new ArrayList<VersioningHandler>();
        versioningHandlers.add( new ValuesVersioningHandler() );
    }

    @Override
    public void onEvent( Event e ) {
//        if( e instanceof PreSaveEvent ) {
//            PreSaveEvent pse = (PreSaveEvent) e;
//            if( pse.getResource() instanceof BaseResource ) {
//                createVersion( (BaseResource) pse.getResource());
//            }
//        }
    }

    private void createVersion( BaseResource r ) {
        NameNode nnVersionParent = null;
        VersionDataNode dn;
        for( VersioningHandler h : versioningHandlers ) {
            if( h.supports( r ) ) {
                if( nnVersionParent == null ) {
                    nnVersionParent = getOrCreateVersionParent( r.getNameNode() );
                }
                dn = h.createVersion( nnVersionParent, r );
                String versionName = "version_" + nnVersionParent.children().size() + 1;
                nnVersionParent.add( versionName, dn );
            }
        }
    }

    private NameNode getOrCreateVersionParent( RelationalNameNode nameNode ) {
        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    public static class ValuesVersioningHandler implements VersioningHandler {

        @Override
        public boolean supports( BaseResource r ) {
            return true;
        }

        @Override
        public VersionDataNode createVersion( NameNode nnVersionParent, BaseResource r ) {
            return null;
        }
    }

    public static class ValuesVersionDataNode implements VersionDataNode {

        private final ComponentValueMap values;

        public ValuesVersionDataNode( BaseResource r ) {
            this.values = r.getValues();

        }

        @Override
        public void revert( NameNode target ) {
        }

        @Override
        public String getChangeSummary() {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        @Override
        public void setId( UUID id ) {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        @Override
        public UUID getId() {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        @Override
        public void init( NameNode nameNode ) {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        @Override
        public void onDeleted( NameNode nameNode ) {
            throw new UnsupportedOperationException( "Not supported yet." );
        }
    }

    public static interface VersionDataNode extends DataNode {

        /**
         * Undo this change 
         * 
         * @param target
         */
        void revert( NameNode target );

        /**
         * 
         * @return - a brief textual description of the change this represents
         */
        String getChangeSummary();
    }

    public static interface VersioningHandler {

        boolean supports( BaseResource r );

        VersionDataNode createVersion( NameNode nnVersionParent, BaseResource r );
    }
}
