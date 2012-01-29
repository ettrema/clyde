package com.ettrema.web.manage.deploy;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import static com.ettrema.context.RequestContext._;
import com.ettrema.vfs.VfsSession;
import com.ettrema.web.ExistingResourceFactory;
import com.ettrema.web.Web;
import com.ettrema.web.manage.synch.FileLoader;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author brad
 */
public class DeployResourceFactory implements ResourceFactory {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DeployResourceFactory.class);
    private final ExistingResourceFactory existingResourceFactory;
    private final DeploymentService deploymentService;
    private String deployFolderName = "_deploy";

    public DeployResourceFactory(ExistingResourceFactory existingResourceFactory, DeploymentService deploymentService) {
        this.existingResourceFactory = existingResourceFactory;
        this.deploymentService = deploymentService;
    }

    @Override
    public Resource getResource(String host, String spath) {
        Path path = Path.path(spath);
        Resource r = find(host, path);
        return r;
    }

    private Resource find(String host, Path path) {
        if (path.getParent().isRoot()) {
            if (path.getName().equals(deployFolderName)) {
                Resource r = existingResourceFactory.getResource(host, path.getParent().toString());
                if (r == null) {
                    return null;
                } else if (r instanceof Web) {
                    Web web = (Web) r;
                    return new DeployFolder(web);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            Resource r = find(host, path.getParent());
            if (r instanceof CollectionResource) {
                CollectionResource parent = (CollectionResource) r;
                return parent.child(path.getName());
            } else {
                return null;
            }
        }
    }

    public class DeployFolder extends AbstractDeploymentResource implements CollectionResource, PutableResource, PropFindableResource, DigestResource {

        private final Web web;

        public DeployFolder(Web web) {
            super(web);
            this.web = web;
        }

        @Override
        public Resource child(String childName) {
            for (Resource r : getChildren()) {
                if (r.getName().equals(childName)) {
                    return r;
                }
            }
            return null;
        }

        @Override
        public List<? extends Resource> getChildren() {
            List<com.ettrema.web.manage.deploy.Deployment> deployments = deploymentService.getDeployments(web);
            List<DeploymentResource> list = new ArrayList<>();
            for (Deployment d : deployments) {
                list.add(new DeploymentResource(web, d));
            }
            return list;
        }

        @Override
        public String getUniqueId() {
            return null;
        }

        @Override
        public String getName() {
            return deployFolderName;
        }

        @Override
        public Date getModifiedDate() {
            return null;
        }

        @Override
        public String checkRedirect(Request request) {
            return null;
        }

        /**
         * Process a deloyment
         *
         * @param newName
         * @param inputStream
         * @param length
         * @param contentType
         * @return
         * @throws IOException
         * @throws ConflictException
         * @throws NotAuthorizedException
         * @throws BadRequestException
         */
        @Override
        public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
            File zippedFile = File.createTempFile("clyde", "deploy-" + newName);
            try (FileOutputStream fout = new FileOutputStream(zippedFile)) {
                BufferedOutputStream bufOut = new BufferedOutputStream(fout);
                long copied = IOUtils.copyLarge(inputStream, bufOut);
                if (length != null) {
                    if (copied != length) {
                        throw new RuntimeException("Uploaded file size does not match saved byte size: saved=" + copied + " uploaded=" + length);
                    }
                }
                log.info("createNew: wrote " + copied + " bytes to temp file");
                bufOut.flush();
                fout.flush();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            try {
                Deployment deployment = deploymentService.deploy(zippedFile, newName, web);
                return new DeploymentResource(web, deployment);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

        }

        @Override
        public Date getCreateDate() {
            return null;
        }
    }

    public class DeploymentResource extends AbstractDeploymentResource implements DeletableResource, PropFindableResource, ReplaceableResource {

        private final Web web;
        private final Deployment deployment;

        public DeploymentResource(Web web, Deployment deployment) {
            super(web);
            this.web = web;
            this.deployment = deployment;
        }

        @Override
        public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
            try {
                deploymentService.undeploy(web, deployFolderName);
                _(VfsSession.class).commit();
            } catch (Exception ex) {
                _(VfsSession.class).rollback();
            }
        }

        @Override
        public String getUniqueId() {
            return deployment.getId().toString();
        }

        @Override
        public String getName() {
            return deployment.getName();
        }

        @Override
        public Date getModifiedDate() {
            return deployment.getModifiedDate();
        }

        @Override
        public String checkRedirect(Request request) {
            return null;
        }

        @Override
        public Date getCreateDate() {
            return deployment.getCreatedDate();
        }

        @Override
        public void replaceContent(InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException {
            try {
                File zippedFile = File.createTempFile("clyde", "deploy-" + getName());
                try (FileOutputStream fout = new FileOutputStream(zippedFile)) {
                    BufferedOutputStream bufOut = new BufferedOutputStream(fout);
                    IOUtils.copyLarge(in, bufOut);
                    try {
                        deploymentService.deploy(zippedFile, getName(), web);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        }
    }

    public abstract class AbstractDeploymentResource implements Resource, DigestResource {

        private final Web web;

        public AbstractDeploymentResource(Web web) {
            this.web = web;
        }

        @Override
        public Object authenticate(String user, String password) {
            return web.authenticate(user, password);
        }

        @Override
        public boolean authorise(Request request, Method method, Auth auth) {
            return web.authorise(request, Method.DELETE, auth);
        }

        @Override
        public Object authenticate(DigestResponse digestRequest) {
            return web.authenticate(digestRequest);
        }

        @Override
        public boolean isDigestAllowed() {
            return web.isDigestAllowed();
        }

        @Override
        public String getRealm() {
            return web.getRealm();
        }
    }

    public String getDeployFolderName() {
        return deployFolderName;
    }

    public void setDeployFolderName(String deployFolderName) {
        this.deployFolderName = deployFolderName;
    }
}
