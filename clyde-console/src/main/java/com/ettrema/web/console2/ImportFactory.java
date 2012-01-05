package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.web.code.CodeResourceFactory;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

/**
 *
 * @author brad
 */
public class ImportFactory extends AbstractFactory{

	private final CodeResourceFactory codeResourceFactory;
	
    public ImportFactory(CodeResourceFactory codeResourceFactory) {
        super( "Import from a remote location. Eg import http://www.host.com/ username password", new String[]{"import","im"});
		this.codeResourceFactory = codeResourceFactory;
    }

	@Override
    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Import(args,host,currentDir,resourceFactory, codeResourceFactory);
    }

}
