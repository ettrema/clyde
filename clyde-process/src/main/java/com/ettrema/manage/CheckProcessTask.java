package com.ettrema.manage;

import com.ettrema.context.Context;
import com.ettrema.grid.Processable;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author brad
 */
public class CheckProcessTask implements Processable, Serializable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CheckProcessTask.class );
    private static final long serialVersionUID = 1L;
    private final String hostName;
    private final Date createdAt;

    public CheckProcessTask( String hostName, Date createdAt ) {
        this.hostName = hostName;
        this.createdAt = createdAt;
    }


    public void doProcess( Context context ) {
        log.debug( "checking process" );
        ProcessQuotaManager quotaManager = context.get( ProcessQuotaManager.class );
        quotaManager.checkProcess( hostName, context, createdAt );

    }

    public void pleaseImplementSerializable() {
    }
}
