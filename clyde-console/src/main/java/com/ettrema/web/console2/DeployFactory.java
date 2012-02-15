
package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import com.ettrema.web.manage.deploy.DeploymentService;
import java.util.List;

public class DeployFactory extends AbstractFactory{

    private final DeploymentService deploymentService;
    
    private String mavenRoot;
    
    public DeployFactory(DeploymentService deploymentService) {
        super("Deploy from a maven repo", new String[]{"deploy"});
        this.deploymentService = deploymentService;
    }

    @Override
    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        Deploy d = new Deploy(args, host, currentDir,resourceFactory, deploymentService);
        d.setMavenRoot(mavenRoot);
        return d;
    }

    public String getMavenRoot() {
        return mavenRoot;
    }

    public void setMavenRoot(String mavenRoot) {
        this.mavenRoot = mavenRoot;
    }
    
    
}
