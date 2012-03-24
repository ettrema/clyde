package com.ettrema.web.console2;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.io.StreamUtils;
import com.ettrema.web.Folder;
import com.ettrema.console.Result;
import com.ettrema.web.manage.deploy.DeploymentService;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class Deploy extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Deploy.class);
    private final DeploymentService deploymentService;
    private String mavenRoot;

    public Deploy(List<String> args, String host, String currentDir, ResourceFactory resourceFactory, DeploymentService deploymentService) {
        super(args, host, currentDir, resourceFactory);
        this.deploymentService = deploymentService;
    }

    @Override
    public Result execute() {
        Folder cur = currentResource();
        if (cur == null) {
            return result("current dir not found: " + currentDir);
        }
        String deployUrl = "";
        if (cur instanceof Folder) {
            Folder col = cur;
            try { 
                if (args.size() == 1) {
                    deployUrl = args.get(0);

                    return deploy(deployUrl, col);

                } else if (args.size() == 3) {
                    if( mavenRoot == null || mavenRoot.length() == 0 ) {
                        return result("Cant deploy maven spec, no maven root has been configred");
                    }
                    String group = args.get(0);
                    String artifact = args.get(1);
                    String version = args.get(2);
                    deployUrl = mavenRoot + "/" + group.replace(".", "/") + "/" + artifact + "/" + version + "/" + artifact + "-" + version + ".war";
                    log.info("Built maven URL: " + deployUrl);
                    return deploy(deployUrl, col);
                } else {
                    return result("Invalid number of arguments. Either give single URL or 3 arguments for a maven vector group artifact id");
                }
            } catch (MalformedURLException ex) {
                return result("Bad url: " + deployUrl);
            } catch (Exception ex) {
                return result("Exception deploying: " + deployUrl, ex);
            }
        } else {
            return result("not a collection: " + cur.getHref());
        }

    }

    private Result deploy(String deployUrl, Folder currentFolder) throws MalformedURLException, IOException, Exception {
        URL url = new URL(deployUrl);

        try (InputStream in = url.openStream(); BufferedInputStream bufIn = new BufferedInputStream(in)) {
            Path path = Path.path(url.getPath());
            File deployWar = File.createTempFile("ettrema-deploy", path.getName());
            try (FileOutputStream fout = new FileOutputStream(deployWar); BufferedOutputStream bufOut = new BufferedOutputStream(fout)) {
                StreamUtils.readTo(bufIn, bufOut);
                bufOut.flush();
                fout.flush();
            }
            String deployName = findDeployName(path);
            deploymentService.deploy(deployWar, deployName, currentFolder.getWeb());
            commit();
            return result("Deployed ok: " + deployName);
        }
    }

    private String findDeployName(Path p) {
        String s = p.getName(); // eg issues-web-1.0.war
        int pos = s.lastIndexOf("-");
        s = s.substring(0, pos);
        return s;

    }

    public String getMavenRoot() {
        return mavenRoot;
    }

    public void setMavenRoot(String mavenRoot) {
        this.mavenRoot = mavenRoot;
    }

}
