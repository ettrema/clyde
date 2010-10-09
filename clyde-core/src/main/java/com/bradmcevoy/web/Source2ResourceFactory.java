
package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.utils.FileUtils;
import com.bradmcevoy.utils.XmlUtils2;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class Source2ResourceFactory extends CommonResourceFactory {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Source2ResourceFactory.class);
    
    public static final String ROOT_FOLDER = "_src";
    
    private ResourceFactory next;
    
    public Source2ResourceFactory() {
        next = new ExistingResourceFactory();
    }
    
    @Override
    public Resource getResource(String host, String url) {
        Path path = Path.path(url);
        String first = path.getFirst();
        if( first == null ) {
//            log.warn("..first is null - " + path);
            return null;
        }
        if( first.equals(ROOT_FOLDER)) {
//            log.debug(".. is source folder");
            path = path.getStripFirst();
            String url2 = path.toString();
            if( path.isRoot() || path.getName() == null ) {
                Resource hostFolder = next.getResource(host, "/");
//                log.debug("..wrapping host");
                return new SourceFolder((Folder) hostFolder);
            } else if( path.getName().endsWith(".meta.xml")) {
//                log.debug("..is meta");
                url2 = url2.replace(".meta.xml", "");
                Resource r = next.getResource(host, url2);
                if( r == null ) {
//                    log.debug("..real resource not found: " + url2);
                    return null;
                } else {
//                    log.debug("..returning meta resource");
                    return new MetaFile((BaseResource) r);
                }
            } else {
//                log.debug("..not meta");
                Resource r = next.getResource(host, url2);
                if( r == null ) return null;
//                log.debug("..got resource: " + r.getName());
                r = createFile( (BaseResource) r);
                return r;
            }
            
        } else {
            return null;
        }
    }    
    
    public abstract class SourceCommon<T extends BaseResource> implements Resource, DeletableResource {

        T res;

        public SourceCommon(T res) {
            this.res = res;
        }

        public SourceCommon() {            
        }
    
        @Override
        public String getUniqueId() {
            return null;
        }
        
        @Override
        public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
            res.delete();
        }
                
        @Override
        public Object authenticate(String user, String password) {
            return res.authenticate(user, password);
        }

        @Override
        public boolean authorise(Request request, Method method, Auth auth) {
            return res.authorise(request, Method.POST, auth);
        }

        @Override
        public String getRealm() {
            return res.getRealm();
        }

        @Override
        public Date getModifiedDate() {
            return res.getModifiedDate();
        }


        @Override
        public String checkRedirect(Request request) {
            return null;
        }

        public Date getCreateDate() {
            return res.getCreateDate();
        }               
    }
    
    public class SourceFolder extends SourceCommon<Folder> implements PropFindableResource, CollectionResource, PutableResource, MakeCollectionableResource {

        public SourceFolder(Folder folder) {
            super(folder);
        }

        @Override
        public String getUniqueId() {
            return null;
        }        
        
        @Override
        public String getName() {
            return res.getName();
        }

        @Override
        public Resource child(String childName) {
            for( Resource r : getChildren() ) {
                if( r.getName().equals(childName)) return r;
            }
            return null;
        }

        @Override
        public List<? extends Resource> getChildren() {
            List<Resource> list = new ArrayList<Resource>();
            for( Resource child : res.getChildren() ) {
                if( child instanceof BaseResource ) {
                    BaseResource baseRes = (BaseResource) child;
                    MetaFile meta = new MetaFile(baseRes);
                    list.add(meta);
                    Resource r = createFile(baseRes);
                    if( r != null ) list.add(r);
                }
            }
//            log.debug("getChildren: " + list.size());
            return list;
        }
        
        @Override
        public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
            if( newName.endsWith(".meta.xml")) {
                newName = newName.replace(".meta.xml", "");
                ByteArrayOutputStream out = FileUtils.readIn(inputStream);
                String xml = out.toString();
                log.debug(xml);
                try {
                    XmlUtils2 x = new XmlUtils2();
                    Document doc = x.getJDomDocument(xml);
                    Element el = doc.getRootElement();
                    el = el.getChild("res");
                    BaseResource resNew = BaseResource.importResource(res, el, newName);
                    resNew.save();
                    commit();
                    return new MetaFile(resNew);
                } catch (JDOMException ex) {
                    throw new RuntimeException(xml, ex);
                }
            } else {
                BaseResource resChild = res.childRes(newName);
                if( resChild != null ) {
                    resChild.setContent(inputStream);
                    resChild.save();
                    commit();
                    return resChild;
                } else {
                    return this.res.createNew(newName, inputStream, length, contentType);                    
                }
            }
        }

        @Override
        public CollectionResource createCollection(String newName) throws ConflictException, NotAuthorizedException, BadRequestException{
            if( newName.endsWith(".meta.xml")) {
                throw new RuntimeException("Can't create a folder with an extension of .meta.xml");
            } else {
                Folder resChild = (Folder) res.createCollection(newName);
                return new SourceFolder(resChild);
            }
            
        }
    }
            
    public SourceCommon createFile(BaseResource baseRes) {
        if( baseRes instanceof Folder ) {
            SourceFolder sf = new SourceFolder((Folder) baseRes);
            return sf;
        } else if( baseRes instanceof BinaryFile ) {
            BinaryContentFile f = new BinaryContentFile((BinaryFile) baseRes);
            return f;
        } else if( baseRes instanceof TextFile ) {
            TextContentFile f = new TextContentFile(   (TextFile) baseRes);
            return f;
        } else {
            return null;
        }
        
    }
    
    public class MetaFile extends SourceCommon implements GetableResource, PropFindableResource {
        public MetaFile(BaseResource file) {
            super(file);
        }
                               
        @Override
        public String getName() {
            return res.getName() + ".meta.xml";
        }

        @Override
        public String getContentType(String accepts) {
            return "text/xml";
        }

        @Override
        public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
            Document doc = new Document();
            Element resRoot = new Element("res");
            doc.addContent(resRoot);
            res.toXml(resRoot);
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            try {
                outputter.output(doc, out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            out.flush();
            out.close();
        }

        @Override
        public Long getMaxAgeSeconds(Auth auth) {
            return null;
        }

        @Override
        public Long getContentLength() {
            return null;
        }
    }    
    
    public class TextContentFile extends SourceCommon<TextFile> implements GetableResource, PropFindableResource {
        public TextContentFile(TextFile file) {
            super(file);
        }                
        
        @Override
        public String getName() {
            return res.getName();
        }

        @Override
        public String getContentType(String accepts) {
            return res.getContentType(accepts);
        }

        @Override
        public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
            String s = res.getContent();
            out.write(s.getBytes());
            out.flush();
            out.close();
        }

        @Override
        public Long getContentLength() {
            return res.getContentLength();
        }
                
        @Override
        public Long getMaxAgeSeconds(Auth auth) {
            return null;
        }        
    }
    
    public class BinaryContentFile extends SourceCommon<BinaryFile> implements GetableResource, PropFindableResource {
        public BinaryContentFile(BinaryFile file) {
            super(file);
        }                
        
        @Override
        public String getName() {
            return res.getName();
        }

        @Override
        public String getContentType(String accepts) {
            return res.getContentType(accepts);
        }

        @Override
        public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
            InputStream in = res.getInputStream();
            StreamUtils.readTo(in, out);
            out.flush();
            out.close();
        }

        @Override
        public Long getMaxAgeSeconds(Auth auth) {
            return null;
        }

        @Override
        public Long getContentLength() {
            return res.getContentLength();
        }
    }                        
}
