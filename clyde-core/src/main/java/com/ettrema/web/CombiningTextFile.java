package com.ettrema.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.logging.LogUtils;
import com.ettrema.web.component.InitUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class CombiningTextFile extends File {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CombiningTextFile.class);
    private static final long serialVersionUID = 1L;
    private List<Path> includes;
    private String includeExt;

    public CombiningTextFile(String contentType, Folder parentFolder, String newName) {
        super(contentType, parentFolder, newName);
    }

    public CombiningTextFile(Folder parentFolder, String newName) {
        super("text", parentFolder, newName);
    }

    @Override
    public String getDefaultContentType() {
        // since binary files can represent many different content types
        // we try to infer from the file name
        return ContentTypeUtil.getContentTypeString(getName());
    }

    @Override
    protected BaseResource newInstance(Folder parent, String newName) {
        return new CombiningTextFile(this.getContentType(null), parent, newName);
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        LogUtils.trace(log, "sendContent: includes=", includes, "includeExt=", includeExt);
        List<GetableResource> list = new ArrayList<>();
        Folder parent = getParent();
        if (includes != null) {
            for (Path includeName : includes) {
                Resource child = parent.find(includeName);
                if (child == null) {
                    log.warn("Couldnt find resource to imclude: " + includeName + " in folder: " + this.getParent().getHref());
                } else if (child instanceof GetableResource) {
                    if (child != this) {
                        GetableResource gr = (GetableResource) child;
                        list.add(gr);
                    }
                } else {
                    log.warn("is not getable! " + child.getClass());
                }
            }
        } else {
            log.warn("no includes defined for combiningtextfile: " + this.getName());
        }

        if (includeExt != null) {
            LogUtils.trace(log, "sendContent: including files with extension", includeExt);
            for (Resource r : parent.getChildren()) {
                if (r.getName().endsWith(includeExt)) {
                    if (r instanceof GetableResource) {
                        if (r != this) {
                            GetableResource gr = (GetableResource) r;
                            if (!list.contains(gr)) {
                                list.add(gr);
                            }
                        }
                    }
                }
            }
        }

        for (GetableResource gr : list) {
            LogUtils.trace(log, "sendContent: including file-", gr.getName());
            if( !(gr instanceof CombiningTextFile) ) { // allowing inclusion of other combiners makes recursion possible - bad news
                gr.sendContent(out, range, params, contentType);
            }
//                    tempOut.write( "\n".getBytes() ); // write CR            
        }
    }

    @Override
    public Date getModifiedDate() {
        Date mostRecent = super.getModifiedDate();
        if (includes != null) {
            for (Path includeName : includes) {
                Resource child = this.getParent().find(includeName);
                if (child instanceof GetableResource) {
                    GetableResource gr = (GetableResource) child;
                    Date mo = gr.getModifiedDate();
                    if (mo != null) {
                        if (mostRecent == null) {
                            mostRecent = mo;
                        } else if (mo.after(mostRecent)) {
                            mostRecent = mo;
                        }
                    }
                }
            }
        }
        return mostRecent;
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    public List<Path> getIncludes() {
        return includes;
    }

    public void setIncludes(List<Path> includes) {
        this.includes = includes;
    }

    @Override
    public void populateXml(Element e2) {
        super.populateXml(e2);
        String s = null;
        if (includes != null) {
            for (Path name : includes) {
                if (s != null) {
                    s += ",";
                } else {
                    s = "";
                }
                s += name;
            }
        }
        InitUtils.setString(e2, "includes", s);
    }

    @Override
    public void loadFromXml(Element el) {
        super.loadFromXml(el);
        String s = el.getAttributeValue("includes");
        setIncludes(s);
    }

    @Override
    public boolean isIndexable() {
        return false;
    }

    public void setIncludes(String s) {
        includes = new ArrayList<>();
        if (s != null && s.trim().length() > 0) {
            String[] arr = s.split(",");
            for (String name : arr) {
                includes.add(Path.path(name));
            }
        }
    }

    public String getIncludeExt() {
        return includeExt;
    }

    public void setIncludeExt(String includeExt) {
        this.includeExt = includeExt;
    }
}
