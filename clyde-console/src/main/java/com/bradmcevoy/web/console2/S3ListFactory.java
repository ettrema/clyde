
package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

public class S3ListFactory extends  AbstractFactory{
    public S3ListFactory() {
        super("List buckets on the current S3 account", new String[]{"s3ls"});
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new S3List(args, host, currentDir,resourceFactory);
    }

}
