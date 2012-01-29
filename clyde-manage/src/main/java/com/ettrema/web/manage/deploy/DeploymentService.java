package com.ettrema.web.manage.deploy;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.utils.ClydeUtils;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.EmptyDataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Web;
import com.ettrema.web.code.CodeResourceFactory;
import com.ettrema.web.manage.synch.DirectFileTransport;
import com.ettrema.web.manage.synch.FileLoader;
import com.ettrema.web.manage.synch.FileScanner;
import java.io.File;
import java.util.*;

/**
 *
 * @author brad
 */
public class DeploymentService {
    public static final String DEPLOYMENTS_NODE_NAME = "_sys_deployments";

    private final ZipService zipService;
    private final CodeResourceFactory codeResourceFactory;

    public DeploymentService(ZipService zipService, CodeResourceFactory codeResourceFactory) {
        this.zipService = zipService;
        this.codeResourceFactory = codeResourceFactory;
    }
    
    public List<Deployment> getDeployments(Web web) {
        NameNode nn = web.getNameNode().child(DEPLOYMENTS_NODE_NAME);
        if( nn == null ) {
            return Collections.EMPTY_LIST;
        }
        List<Deployment> list = new ArrayList<>();
        for(NameNode nnDep : nn.children()) {
            DataNode dn = nnDep.getData();
            if( dn instanceof Deployment ) {
                list.add((Deployment)dn);
            }
        }
        return list;
    }

    public Deployment deploy(File war, String name, Web web) throws Exception {
        
        Deployment previousDeployment = getPreviousDeployment(web, name);
        if( previousDeployment != null ) {
            undeploy(previousDeployment);
        }
        
        File tmp = zipService.getTempDir();
        File unzipped = new File(tmp, name);
        if (unzipped.exists()) {
            if (!unzipped.delete()) {
                throw new RuntimeException("Couldnt delete previous deployment temp dir: " + unzipped.getAbsolutePath());
            }
        }
        if (!unzipped.mkdirs()) {
            throw new RuntimeException("Couldnt create temporary deployment directory: " + unzipped.getAbsolutePath());
        }

        zipService.unzip(war, unzipped);

        String hostName = web.getHost().getName();
        DeploymentFileLoadCallBack callBack = new DeploymentFileLoadCallBack();
        DirectFileTransport fileTransport = new DirectFileTransport(hostName, codeResourceFactory, callBack);
        HtmlErrorReporter errorReporter = new HtmlErrorReporter();
        FileLoader fl = new FileLoader(errorReporter, fileTransport);
        // null rootContext means it won't do individual transactions per file
        FileScanner fileScanner = new FileScanner(null, fl);
        fileScanner.initialScan(true, tmp);

        List<DeploymentItem> items = callBack.asList();
        
        return save(web, name, items);
        
    }

    public void undeploy(Web web, String name) throws Exception {
        Deployment previousDeployment = getPreviousDeployment(web, name);
        if( previousDeployment != null ) {
            undeploy(previousDeployment);
        }
        previousDeployment.delete(); 
    }

    private void undeploy(Deployment previousDeployment) throws Exception {
        for( DeploymentItem item : previousDeployment.getItems()) {
            BaseResource res = ClydeUtils.loadResource(item.getItemId());
            try {
                res.deleteNoTx();
            } catch (NotAuthorizedException | ConflictException | BadRequestException ex) {
                throw new Exception(ex);
            }
        }
    }
    

    private Deployment save(Web web, String name, List<DeploymentItem> items) {
        NameNode nn = web.getNameNode().child(DEPLOYMENTS_NODE_NAME);
        if( nn == null ) {
            nn = web.getNameNode().add(DEPLOYMENTS_NODE_NAME, new EmptyDataNode());
        }
        NameNode nnDeployment = nn.child(name);
        if( nnDeployment == null ) {
            Deployment deployment = new Deployment();
            deployment.setItems(items);
            nn.add(name, deployment);            
            return deployment;
        } else {
            Deployment deployment = (Deployment) nnDeployment.getData();
            deployment.setItems(items);
            nnDeployment.save();
            return deployment;
        }
    }
    
    private Deployment getPreviousDeployment(Web web, String name) {
        NameNode nn = web.getNameNode().child(DEPLOYMENTS_NODE_NAME);
        if( nn == null ) {
            return null;
        }
        NameNode nnDeployment = nn.child(name);
        if( nnDeployment == null ) {
            return null;
        }
        Deployment deployment = (Deployment) nnDeployment.getData();
        return deployment;
    }    
    
    private class DeploymentFileLoadCallBack implements DirectFileTransport.FileLoadCallback {

        Set<UUID> ids = new HashSet<>();
        
        @Override
        public void onLoaded(Resource r) {
            if( r instanceof BaseResource) {
                BaseResource br = (BaseResource) r;
                ids.add(br.getNameNodeId());
            }
        }

        @Override
        public void onDeleted(Resource r) {
            
        }

        @Override
        public void onModified(Resource r) {
            if( r instanceof BaseResource) {
                BaseResource br = (BaseResource) r;
                ids.add(br.getNameNodeId());
            }
        }

        private List<DeploymentItem> asList() {
            List<DeploymentItem> list = new ArrayList<>();
            for( UUID id : ids ) {
                list.add(new DeploymentItem(id));
            }
            return list;
        }
        
    }
}
