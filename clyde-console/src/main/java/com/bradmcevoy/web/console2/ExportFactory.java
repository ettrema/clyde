package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.web.code.CodeResourceFactory;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

/**
 *
 * @author brad
 */
public class ExportFactory extends AbstractFactory{

    private CodeResourceFactory codeResourceFactory;

    public ExportFactory(CodeResourceFactory codeResourceFactory) {
        super("Export the contents of the current folder recurisvely to a remote host. Optional -dry to do a dry run only. Option -force to export export-disabled resources. The destination path should already exist. Eg export -dry -r -nohost http://www.bradmcevoy.com/somewhere aUser aPassword", new String[]{"export","ex"} );
        this.codeResourceFactory = codeResourceFactory;
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Export(args,host,currentDir, resourceFactory, codeResourceFactory);
    }
}
