
package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

public class S3CopyFactory extends  AbstractFactory{
    public S3CopyFactory() {
        super("Copy data between buckets", new String[]{"s3cp"});
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new S3Copy(args, host, currentDir,resourceFactory);
    }

}
