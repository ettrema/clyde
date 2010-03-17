package com.bradmcevoy.web;

import com.bradmcevoy.common.FrameworkBase;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.utils.XmlUtils2;
import com.bradmcevoy.vfs.VfsCommon;
import com.bradmcevoy.web.security.ClydeAuthoriser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;


public class SourcePage extends VfsCommon implements GetableResource, EditableResource, Replaceable, DigestResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SourcePage.class);
    public final XmlPersistableResource res;

    public SourcePage(XmlPersistableResource res) {
        this.res = res;
    }

    @Override
    public void replaceContent(InputStream in, Long contentLength) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamUtils.readTo(in, out);
            XmlUtils2 x = new XmlUtils2();
            String s = out.toString();
            Document doc = x.getJDomDocument(s);
            Element el = doc.getRootElement();
            el = (Element) el.getChildren().get(0);
            SourcePage.this.res.loadFromXml(el, null);
            SourcePage.this.res.save();
            SourcePage.this.commit();
        } catch (JDOMException ex) {
            throw new RuntimeException(ex);
        } catch (ReadingException ex) {
            throw new RuntimeException(ex);
        } catch (WritingException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getUniqueId() {
        return res.getUniqueId() + "_source";
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
        ContentSender cs = new ContentSender();
        cs.send(out, params);
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getName() {
        return res.getName() + ".source";
    }

    @Override
    public Object authenticate(String user, String password) {
        return res.authenticate( user, password );
    }

    @Override
    public Object authenticate( DigestResponse digestRequest ) {
        return res.authenticate( digestRequest );
    }



    @Override
    public boolean authorise(Request request, Request.Method method, Auth auth) {
        ClydeAuthoriser authoriser = requestContext().get( ClydeAuthoriser.class);
        return authoriser.authorise( this, request);
    }

    @Override
    public String getRealm() {
        return res.getRealm();
    }

    @Override
    public Date getModifiedDate() {
        Date dt = res.getModifiedDate();
        return dt;
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public String getContentType(String accepts) {
        return "text/xml";
    }

    @Override
    public String checkRedirect(Request request) {
        return null;
    }

    static boolean isSourcePath(Path path) {
        if (path == null || path.getName() == null) {
            return false;
        }
        return path.getName().endsWith(".source");
    }

    static Path getPagePath(Path path) {
        String nm = path.getName().replace(".source", "");
        return path.getParent().child(nm);
    }

    class ContentSender extends FrameworkBase {

        void send(OutputStream out, Map<String, String> parameters) {
            DocType docType = new DocType("res",
                 "-//W3C//ENTITIES Latin 1 for XHTML//EN",
                 "http://www.w3.org/TR/xhtml1/DTD/xhtml-lat1.ent");

            Document doc = new Document(new Element("res"), docType);
            res.toXml(doc.getRootElement(), parameters);
            utilXml().saveXMLDocument(out, doc);
        }
    }

    @Override
    public PostableResource getEditPage() {
        return new SourceEditPage();
    }

    public class SourceEditPage extends FrameworkBase implements GetableResource, PostableResource, DigestResource {

        public String err;

        @Override
        public String getUniqueId() {
            return SourcePage.this.getUniqueId() + "_edit";
        }

        @Override
        public String processForm(Map<String, String> parameters, Map<String, FileItem> files) {
            String s = parameters.get("source");
            String cmd = parameters.get("command");
            if (cmd == null) {
                log.warn("No command");
                return null;
            }
            try {
                log.debug("processForm: " + s.length() + " - " + cmd);
                if (cmd.equals("Save")) {
                    XmlUtils2 x = new XmlUtils2();
                    Document doc = x.getJDomDocument(s);
                    Element el = doc.getRootElement();
                    el = (Element) el.getChildren().get(0);
                    SourcePage.this.res.loadFromXml(el, parameters);
                    SourcePage.this.res.save();
                    SourcePage.this.commit();
                    log.debug("saved ok");
                    return null;
                } else if (cmd.equals("Delete")) {
                    String redirectTo = null;
                    if (SourcePage.this.res instanceof CommonTemplated) {
                        CommonTemplated page = (CommonTemplated) SourcePage.this.res;
                        CommonTemplated parent = page.getParent();
                        if (parent != null) {
                            redirectTo = parent.getHref();
                        }
                    }
                    SourcePage.this.res.delete();
                    SourcePage.this.commit();
                    log.debug("deleted");
                    return redirectTo;
                }
            } catch (Throwable ex) {
                log.error("Exception saving source", ex);
                err = ex.getClass() + " - " + ex.getLocalizedMessage();
                RequestParams.current().attributes.put("source", s);
                RequestParams.current().attributes.put("ex", ex);
            }
            return null;
        }

        @Override
        public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {

            PrintWriter pw = new PrintWriter(out);
            pw.println("<html>");
            pw.println("<head>");
            pw.println("<title>Source: " + SourcePage.this.res.getName() + "</title>");
            pw.println("</head>");
            pw.println("<body>");
            pw.println("<h1><a href='" + SourcePage.this.res.getHref() + "'>" + SourcePage.this.res.getHref() + "</a></h1>");
            if (SourcePage.this.res instanceof CommonTemplated) {
                CommonTemplated ct = (CommonTemplated) SourcePage.this.res;
                ITemplate t = ct.getTemplate();
                if (t != null) {
                    pw.println("<h2>Template: <a href='" + t.getHref() + "'>" + t.getHref() + "</a></h2>"); 
                }
            }
            if (err != null) {
                pw.println("<font color='RED'>" + err + "</font>");
                pw.println("<br/>");
            }
            pw.println("<form action='' method='POST'>");
            pw.println("<textarea wrap='off' name='source' style='height: 80%; width: 100%;'>");
            sendXml(pw, params);
            pw.println("</textarea>");
            pw.println("<br/>");
            pw.println("<input type='submit' name='command' value='Save'/>");
            pw.println("<input type='submit' name='command' value='Delete' onclick=\"return confirm('Are you sure you want to delete this resource?')\"/>");
            pw.println("</form>");
            Exception e = (Exception) RequestParams.current().attributes.get("ex");
            if (e != null) {
                pw.println("<pre>");
                e.printStackTrace(pw);
                pw.println("</pre>");
            }
            pw.println("</body></html>");
            pw.flush();
            pw.close();
        }

        private void sendXml(PrintWriter pw, Map<String, String> params) {
            String source = (String) RequestParams.current().attributes.get("source");
            if (source != null) {
                pw.print(source);
            } else {
                XmlUtils2 x = new XmlUtils2();
                Document doc = new Document(new Element("res"));
                SourcePage.this.res.toXml(doc.getRootElement(), params);
                utilXml().transformDocument(pw, doc);
            }
        }

        @Override
        public Long getMaxAgeSeconds(Auth auth) {
            return null;
        }

        @Override
        public String getName() {
            return SourcePage.this.getName() + ".edit";
        }

        @Override
        public Object authenticate(String user, String password) {
            return SourcePage.this.authenticate(user, password);
        }

        @Override
        public Object authenticate( DigestResponse digestRequest ) {
            return SourcePage.this.authenticate(digestRequest);
        }

        @Override
        public boolean authorise(Request request, Method method, Auth auth) {
            return (auth != null);
        }

        @Override
        public String getRealm() {
            return SourcePage.this.getRealm();
        }

        @Override
        public Date getModifiedDate() {
            return SourcePage.this.getModifiedDate();
        }

        @Override
        public Long getContentLength() {
            return null;
        }

        @Override
        public String getContentType(String accepts) {
            return "text/html";
        }

        @Override
        public String checkRedirect(Request request) {
            return null;
        }

        public int compareTo(Object o) {
            if (o instanceof Resource) {
                Resource res = (Resource) o;
                return this.getName().compareTo(res.getName());
            } else {
                return -1;
            }
        }
    }
}
