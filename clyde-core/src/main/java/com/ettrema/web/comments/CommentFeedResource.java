package com.ettrema.web.comments;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.ettrema.web.Folder;
import com.ettrema.web.IUser;
import com.ettrema.web.security.ClydeAuthenticator;
import com.ettrema.web.security.ClydeAuthoriser;
import com.ettrema.context.RequestContext;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;

import static com.ettrema.context.RequestContext._;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import com.ettrema.web.BaseResource;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;
import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;

/**
 * Provides fast, denormalised access to comments for a folder structure
 *
 * Has specific support for images and pages
 *
 */
public class CommentFeedResource implements GetableResource, DigestResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CommentFeedResource.class);
    public static final String PATTERN_RESPONSE_HEADER = "E, dd MMM yyyy HH:mm:ss Z"; // Tue, 29 Jun 2010 10:37:14 +1200
    private final String name;
    private final CommentDao commentDao;
    private final BaseResource baseResource;
    private final Long cacheSeconds;
    private final String basePath;
    private int itemCount;
    private int itemsFound;

    public CommentFeedResource(CommentDao commentDao, String name, BaseResource baseResource, Long cacheSeconds, String basePath) {
        this.name = name;
        this.commentDao = commentDao;
        this.baseResource = baseResource;
        this.cacheSeconds = cacheSeconds;
        this.basePath = basePath;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
        int page = getIntParam(params, "page", 0);
        final int pageSize = getIntParam(params, "pageSize", 10);
        final int skip = page < 10 ? page * 2 : 20; //
        log.warn("sendContent: page:" + page);

        final List<CommentBean> list = new ArrayList<>();
        commentDao.search(new CommentCollector() {

            @Override
            public boolean onResult(UUID commentNameId, Date datePosted, String pagePath) {
                Comment c = loadComment(commentNameId);
                if (c != null) {
                    itemCount++;
                    if (itemCount >= skip) {
                        itemsFound++;
                        CommentBean bean = new CommentBean();
                        bean.setComment(c.getComment());
                        bean.setUser(c.getUser());
                        bean.setDate(datePosted);
                        bean.setPagePath(pagePath);
                        String title = c.page().getTitle();
                        bean.setPageTitle(title);
                        list.add(bean);
                    }
                }
                return itemCount < pageSize;
            }
        }, baseResource);
        JsonConfig cfg = new JsonConfig();
        cfg.setIgnoreTransientFields(true);
        cfg.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);

        JSON json = JSONSerializer.toJSON(list, cfg);
        Writer writer = new PrintWriter(out);
        json.write(writer);
        writer.flush();
    }

    private Comment loadComment(UUID id) {
        NameNode nn = _(VfsSession.class).get(id);
        if (nn == null) {
            return null;
        }
        DataNode dn = nn.getData();
        if (dn == null) {
            return null;
        }
        if (dn instanceof Comment) {
            Comment cDn = (Comment) dn;
            return cDn;
        } else {
            return null;
        }
    }

    public static class CommentBean {

        private UserBean user;
        private String pagePath;
        private String pageTitle;
        private Date date;
        private String comment;

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public UserBean getUser() {
            return user;
        }

        public void setUser(UserBean user) {
            this.user = user;
        }

        /**
         * @return the pagePath
         */
        public String getPagePath() {
            return pagePath;
        }

        /**
         * @param pagePath the pagePath to set
         */
        public void setPagePath(String pagePath) {
            this.pagePath = pagePath;
        }

        /**
         * @return the pageTitle
         */
        public String getPageTitle() {
            return pageTitle;
        }

        /**
         * @param pageTitle the pageTitle to set
         */
        public void setPageTitle(String pageTitle) {
            this.pageTitle = pageTitle;
        }

        /**
         * @return the datePosted
         */
        public Date getDate() {
            return date;
        }

        /**
         * @param datePosted the datePosted to set
         */
        public void setDate(Date datePosted) {
            this.date = datePosted;
        }
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return cacheSeconds;
    }

    @Override
    public String getContentType(String accepts) {
        return "application/x-javascript; charset=utf-8";
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {
        ClydeAuthoriser authoriser = requestContext().get(ClydeAuthoriser.class);
        return authoriser.authorise(baseResource, request, method, auth);
    }

    @Override
    public String getRealm() {
        return baseResource.getName();
    }

    @Override
    public Date getModifiedDate() {
        return null;
    }

    @Override
    public String checkRedirect(Request request) {
        return null;
    }

    @Override
    public IUser authenticate(String user, String password) {
        ClydeAuthenticator authenticator = requestContext().get(ClydeAuthenticator.class);
        IUser o = authenticator.authenticate(baseResource, user, password);
        if (o == null) {
            log.warn("authentication failed by: " + authenticator.getClass());
        }
        return o;
    }

    @Override
    public Object authenticate(DigestResponse digestRequest) {
        ClydeAuthenticator authenticator = requestContext().get(ClydeAuthenticator.class);
        Object o = authenticator.authenticate(baseResource, digestRequest);
        if (o == null) {
            log.warn("authentication failed by: " + authenticator.getClass());
        }
        return o;
    }

    @Override
    public boolean isDigestAllowed() {
        return baseResource.isDigestAllowed();
    }

    protected RequestContext requestContext() {
        return RequestContext.getCurrent();
    }

    private String toFolderPath(String basePath) {
        int pos = basePath.lastIndexOf("/");
        return basePath.substring(0, pos);
    }

    private String getTitleFromDate(Date dateTaken) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateTaken);
        String s = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH) + " " + cal.get(Calendar.YEAR);
        return s;
    }

    private int getIntParam(Map<String, String> params, String name, int defValue) throws NumberFormatException {
        String sPage = params.get(name);
        int page;
        if (StringUtils.isEmpty(sPage)) {
            page = defValue;
        } else {
            page = Integer.parseInt(sPage);
        }
        return page;
    }

    private boolean getBoolParam(Map<String, String> params, String string) {
        String s = params.get(name);
        boolean b;
        if (StringUtils.isEmpty(s)) {
            b = false;
        } else {
            b = Boolean.parseBoolean(s);
        }
        return b;

    }
}
