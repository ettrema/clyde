package com.bradmcevoy.web;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.utils.AuthoringPermissionService;
import com.bradmcevoy.web.security.PermissionChecker;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import com.ettrema.context.RequestContext;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import static com.ettrema.context.RequestContext._;

public class SimpleEditPage implements GetableResource, PostableResource {

    final SimpleEditable editable;

    public SimpleEditPage(SimpleEditable editable) {
        this.editable = editable;
    }

    @Override
    public String getUniqueId() {
        return editable.getUniqueId() + "_simpleedit";
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
        PrintWriter pw = new PrintWriter(out);
        pw.println("<html>");
        pw.println("<body>");
        pw.println("<form method='POST' action='" + this.getName() + "'>");
        pw.println("<input type='submit' name='Save' value='save'>");
        pw.println("<br/>");
        pw.println("<textarea name='text' style='height: 90%; width: 100%;'>");
        pw.print(editable.getContent());
        pw.println("</textarea>");
        pw.println("<br/>");
        pw.println("<input type='submit' name='Save' value='save'>");
        pw.println("</form>");
        pw.println("</body>");
        pw.println("</html>");
        pw.flush();
        pw.close();
    }

    @Override
    public String processForm(Map<String, String> parameters, Map<String, FileItem> files) {
        if (parameters.containsKey("Save")) {
            editable.setContent(parameters.get("text"));
            editable.save();
            editable.commit();
        } else {
            System.out.println("no save command");
        }
        return null;
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    public String getHref() {
        return editable.getHref();
    }

    @Override
    public String getName() {
        return editable.getName() + EditPage.EDIT_SUFFIX;
    }

    @Override
    public Object authenticate(String user, String password) {
        return editable.authenticate(user, password);
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {
        PermissionChecker permissionChecker = RequestContext.getCurrent().get( PermissionChecker.class );
        Role editingRole = _(AuthoringPermissionService.class).getEditRole( editable );
        return permissionChecker.hasRole( editingRole, editable, auth );
    }

    @Override
    public String getRealm() {
        return editable.getRealm();
    }

    @Override
    public Date getModifiedDate() {
        return editable.getModifiedDate();
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

    public static interface SimpleEditable extends Templatable {

        void setContent(String content);

        String getContent();

        void save();

        void commit();
    }
}
