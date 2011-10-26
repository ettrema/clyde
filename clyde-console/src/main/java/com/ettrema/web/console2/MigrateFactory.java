package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import com.ettrema.vfs.JdbcBinaryManager;
import com.ettrema.vfs.aws.AwsBinaryManager;
import java.util.List;

/**
 *
 * @author brad
 */
public class MigrateFactory  extends AbstractFactory{

    private final JdbcBinaryManager from;
    private final AwsBinaryManager to;

    public MigrateFactory(JdbcBinaryManager from,AwsBinaryManager to) {
        super( "Migrate data from database to s3", new String[]{"migrate"} );
        this.from = from;
        this.to = to;

    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Migrate(args, host, currentDir, resourceFactory,from, to );

    }


}
