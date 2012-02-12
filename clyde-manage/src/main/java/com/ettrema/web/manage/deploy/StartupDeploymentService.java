package com.ettrema.web.manage.deploy;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.common.Service;
import com.ettrema.context.Context;
import com.ettrema.context.Executable2;
import com.ettrema.context.RootContextLocator;
import com.ettrema.web.ExistingResourceFactory;
import com.ettrema.web.Host;
import java.io.File;

/**
 * Deploys WAR files on application startup
 *
 * @author brad
 */
public class StartupDeploymentService implements Service {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(StartupDeploymentService.class);
    private final DeploymentService deploymentService;
    private final ExistingResourceFactory resourceFactory;
    private final RootContextLocator rootContextLocator;
    private File warFiles;
    private String hostName;

    public StartupDeploymentService(DeploymentService deploymentService, ExistingResourceFactory resourceFactory, RootContextLocator rootContextLocator) {
        this.deploymentService = deploymentService;
        this.resourceFactory = resourceFactory;
        this.rootContextLocator = rootContextLocator;
    }

    @Override
    public void start() {
        if (warFiles == null) {
            log.info("No directory specified for startup deployments");
            return;
        } else if (!warFiles.exists() || !warFiles.isDirectory() || warFiles.listFiles() == null || warFiles.listFiles().length == 0) {
            log.info("No valid directory for startup deployments: " + warFiles.getAbsolutePath());
            return;
        }

        log.info("Deploying from: " + warFiles.getAbsolutePath());

        for (File f : warFiles.listFiles()) {
            if (f.isFile() && f.getName().endsWith(".war")) {
                final File warFile = f;
                rootContextLocator.getRootContext().execute(new Executable2() {

                    @Override
                    public void execute(Context context) {
                        String deployName = findDeployName(warFile);
                        log.info("Auto-deploying: name: " + deployName + " from " + warFile.getAbsolutePath());

                        Host host = findHost();
                        try {
                            deploymentService.deploy(warFile, deployName, host);
                        } catch (NotAuthorizedException | BadRequestException ex) {
                            throw new RuntimeException(ex);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }

                    }
                });
            }
        }
    }

    @Override
    public void stop() {
    }

    private Host findHost() {
        try {
            Resource rHost = resourceFactory.getResource(hostName, "/");
            if (rHost == null) {
                throw new RuntimeException("Couldnt find host with name: " + hostName);
            }
            if (rHost instanceof Host) {
                Host host = (Host) rHost;
                return host;
            } else {
                throw new RuntimeException("Resource is not a: " + Host.class + " is a: " + rHost.getClass());
            }
        } catch (NotAuthorizedException | BadRequestException ex) {
            throw new RuntimeException(ex);
        }

    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public File getWarFiles() {
        return warFiles;
    }

    public void setWarFiles(File warFiles) {
        this.warFiles = warFiles;
    }

    private String findDeployName(File f) {
        String s = f.getName(); // eg issues-web-1.0.war
        int pos = s.lastIndexOf("-");
        s = s.substring(0, pos);
        return s;

    }
}
