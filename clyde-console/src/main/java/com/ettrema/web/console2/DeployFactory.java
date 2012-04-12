
package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import com.ettrema.underlay.UnderlayLocator;
import com.ettrema.web.manage.deploy.DeploymentService;
import java.util.List;

public class DeployFactory extends AbstractFactory{

    private final DeploymentService deploymentService;
    
    private final UnderlayLocator underlayLocator;
    
    private String mavenRoot;
    
    public DeployFactory(DeploymentService deploymentService, UnderlayLocator underlayLocator) {
        super("Deploy from a maven repo, optionally as an underlay Eg deploy -underlay com.ettrema my-web 1.0.1", new String[]{"deploy"});
        this.deploymentService = deploymentService;
        this.underlayLocator = underlayLocator;
    }

    @Override
    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        Deploy d = new Deploy(args, host, currentDir,resourceFactory, deploymentService, underlayLocator);
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
