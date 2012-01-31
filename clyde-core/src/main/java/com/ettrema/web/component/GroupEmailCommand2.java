package com.ettrema.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.io.BufferingOutputStream;
import com.bradmcevoy.property.BeanPropertyResource;
import com.ettrema.context.Context;
import com.ettrema.context.Executable2;
import static com.ettrema.context.RequestContext._;
import com.ettrema.context.RootContext;
import com.ettrema.context.RootContextLocator;
import com.ettrema.mail.MailboxAddress;
import com.ettrema.mail.StandardMessage;
import com.ettrema.mail.StandardMessageImpl;
import com.ettrema.mail.send.MailSender;
import com.ettrema.utils.ClydeUtils;
import com.ettrema.utils.LogUtils;
import com.ettrema.web.*;
import com.ettrema.web.eval.EvalUtils;
import com.ettrema.web.eval.Evaluatable;
import com.ettrema.web.groups.GroupService;
import com.ettrema.web.mail.RetryingMailService;
import com.ettrema.web.mail.RetryingMailService.DelayMessage;
import com.ettrema.web.mail.RetryingMailService.EmailResultCallback;
import com.ettrema.web.mail.RetryingMailService.SendJob;
import com.ettrema.web.security.BeanProperty;
import com.ettrema.web.security.CurrentUserService;
import com.ettrema.web.security.PermissionRecipient.Role;
import com.ettrema.web.security.UserGroup;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import javax.mail.MessagingException;
import org.apache.commons.io.IOUtils;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * An email command which sends to a group, rather then a particular user
 *
 * @author brad
 */
@BeanPropertyResource(value = "clyde")
public final class GroupEmailCommand2 extends Command implements Resource, DigestResource, GetableResource, PropFindableResource, ComponentEx {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GroupEmailCommand2.class);
    private static final long serialVersionUID = 1L;
    public static final Namespace NS = Namespace.getNamespace("c", "http://clyde.ettrema.com/ns/core");
    private Evaluatable bodyText;
    private Evaluatable bodyHtml;
    private Evaluatable from;
    private Evaluatable toGroup;
    private Evaluatable subject;
    private Evaluatable replyTo;
    private Evaluatable confirmationUrl;
    private Evaluatable attachments;

    public GroupEmailCommand2(Addressable container, String name) {
        super(container, name);
    }

    public GroupEmailCommand2(Addressable container, Element el) {
        super(container, el);
        parseXml(el);
    }

    @BeanProperty(readRole = Role.AUTHENTICATED, writeRole = Role.AUTHENTICATED)
    public boolean isRunning() {
        String href = BaseResource.getTargetContainer().getHref();
        SendJob job = _(RetryingMailService.class).getJob(href);
        if (job == null) {
            return false;
        } else {
            return !job.isComplete();
        }
    }

    public void setRunning(boolean b) {
        BaseResource targetPage = BaseResource.getTargetContainer();
        String href = targetPage.getHref();
        if (b) {
            LogUtils.info(log, "setRunning - start", href);
            RenderContext rc = new RenderContext(targetPage.getTemplate(), targetPage, null, false);
            doProcess(rc, null, null);
        } else {
            LogUtils.info(log, "setRunning - cancel", href);
            _(RetryingMailService.class).cancel(href);
        }
    }

    @BeanProperty(readRole = Role.AUTHENTICATED, writeRole = Role.AUTHENTICATED)
    public List<SendStatus> getStatus() {
        String href = BaseResource.getTargetContainer().getHref();
        SendJob job = _(RetryingMailService.class).getJob(href);
        if (job == null) {
            return null;
        } else {
            List<SendStatus> list = new ArrayList<SendStatus>();
            for (RetryingMailService.DelayMessage msg : job.getMsgs()) {
                String email = toEmail(msg);
                String status = toStatus(msg);
                SendStatus ss = new SendStatus(email, msg.getAttempts(), status);
                list.add(ss);
            }
            return list;
        }
    }

    public void parseXml(Element el) {
        bodyText = EvalUtils.getEval(el, "bodyText", NS, container);
        bodyHtml = EvalUtils.getEval(el, "bodyHtml", NS, container);
        from = EvalUtils.getEval(el, "from", NS, container);
        toGroup = EvalUtils.getEval(el, "toGroup", NS, container);
        subject = EvalUtils.getEval(el, "subject", NS, container);
        replyTo = EvalUtils.getEval(el, "replyTo", NS, container);
        confirmationUrl = EvalUtils.getEval(el, "confirmationUrl", NS, container);
        attachments = EvalUtils.getEval(el, "attachments", NS, container);
    }

    @Override
    public void populateXml(Element e2) {
        super.populateXml(e2);
        populateLocalXml(e2);
    }

    public void populateLocalXml(Element e2) {
        EvalUtils.setEval(e2, "bodyText", bodyText, NS);
        EvalUtils.setEval(e2, "bodyHtml", bodyHtml, NS);
        EvalUtils.setEval(e2, "from", from, NS);
        EvalUtils.setEval(e2, "toGroup", toGroup, NS);
        EvalUtils.setEval(e2, "subject", subject, NS);
        EvalUtils.setEval(e2, "replyTo", replyTo, NS);
        EvalUtils.setEval(e2, "confirmationUrl", confirmationUrl, NS);
        EvalUtils.setEval(e2, "attachments", attachments, NS);

    }

    @Override
    public String renderEdit(RenderContext rc) {
        return "todo";
    }

    private MailboxAddress getFrom(RenderContext rc) {
        String s = EvalUtils.evalToString(from, rc, container);
        return MailboxAddress.parse(s);
    }

    private List<User> getTo(RenderContext rc) {
        Object o = EvalUtils.eval(toGroup, rc, rc.page);
        if (o == null) {
            log.warn("getTo: 'to' expression evaluated to null");
        } else {
            LogUtils.trace(log, "getTo: 'to' evaluated to a", o.getClass());
        }
        List<User> list = new ArrayList<User>();
        appendUsers(list, o);
        return list;
    }

    private void appendUsers(List<User> list, Object o) {
        if (o == null) {
            log.warn("Got null value from 'to' expression");
        } else if (o instanceof String) {
            String itemName = (String) o;
            LogUtils.trace(log, "appendUsers: got a string to resolve: ", itemName);
            GroupService groupService = _(GroupService.class);
            UserGroup group = groupService.getGroup((Resource) this.getContainer(), itemName);
            if (group != null) {
                appendUsers(list, group);
            } else {
                // could also be a user name
                if (this.getContainer() instanceof Templatable) {
                    Templatable parent = (Templatable) this.getContainer();
                    User user = parent.getHost().findUser(itemName);
                    if (user != null) {
                        list.add(user);
                    }
                }
            }
        } else if (o instanceof User) {
            User g = (User) o;
            list.add(g);
        } else if (o instanceof Group) {
            Group g = (Group) o;
            list.addAll(g.getMembers());
        } else if (o instanceof List) {
            List items = (List) o;
            LogUtils.trace(log, "appendTo: list of size", items.size());
            for (Object oChild : items) {
                appendUsers(list, oChild);
            }
        } else {
            throw new RuntimeException("Un-supported group type: " + o.getClass().getName());
        }
    }

    /**
     * If testOnly returns the current user's email address!!!
     *
     * @param user - the user to normally return the email address of
     * @param testOnly
     * @return
     */
    private MailboxAddress getAddress(User user, boolean testOnly) {
        if (testOnly) {
            user = (User) _(CurrentUserService.class).getOnBehalfOf();
        }
        String sEmail = user.getExternalEmailTextV2("default");
        if (sEmail != null && sEmail.length() > 0) {
            MailboxAddress add = null;
            try {
                add = MailboxAddress.parse(sEmail);
                return add;
            } catch (IllegalArgumentException e) {
                log.error("Couldnt parse: " + sEmail, e);
                return null;
            }
        } else {
            return null;
        }
    }

    private String getSubject(RenderContext rc) {
        String s = EvalUtils.evalToString(subject, rc, container);
        return s;
    }

    public MailboxAddress getReplyTo(RenderContext rc) {
        String s = EvalUtils.evalToString(replyTo, rc, container);
        return MailboxAddress.parse(s);
    }

    @Override
    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) throws NotAuthorizedException {
        ProcessResult result = onProcessEx(rc, parameters, files);
        if (result != null) {
            return result.getRedirect();
        } else {
            return null;
        }
    }

    @Override
    public ProcessResult onProcessEx(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) throws NotAuthorizedException {
        log.trace("onProcess");
        String s = parameters.get(this.getName());
        if (s == null) {
            return null; // not this command
        }
        log.trace("onProcess2");
        if (!validate(rc)) {
            log.debug("validation failed");
            return null;
        }
        String redirect = doProcess(rc, parameters, files);
        return new ProcessResult(redirect, null, true);
    }

    public SendJob getJob(Templatable targetPage) {
        return _(RetryingMailService.class).getJob(targetPage.getHref());

    }

    @Override
    protected String doProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        boolean testOnly = isTestOnly(parameters);
        if (testOnly) {
            log.info("doProcess - test Only");
        } else {
            log.info("doProcess - real send");
        }
        try {
            send(rc, testOnly);
            String url = EvalUtils.evalToString(confirmationUrl, rc, container);
            if (url != null && url.length() > 0) {
                return url;
            } else {
                return null;
            }
        } catch (MessagingException ex) {
            log.error("exception sending email", ex);
            return null;
        }
    }

    @Override
    public boolean validate(RenderContext rc) {
        return true;
    }

    public void send(RenderContext rc, boolean testOnly) throws MessagingException {
        log.info("send: test=" + testOnly + " -----------------------------------");
        List<User> recipList = getTo(rc);
        List<StandardMessage> msgs = new ArrayList<StandardMessage>();
        for (User user : recipList) {
            MailboxAddress address = getAddress(user, testOnly); // returns current user's email if testObly
            if (address != null) {
                log.info("send email to: " + address + " ...");
                StandardMessage sm = new StandardMessageImpl();
                rc.getAttributes().put("recipient", user);
                sm.setText(EvalUtils.evalToString(bodyText, rc, container));
                sm.setHtml(EvalUtils.evalToString(bodyHtml, rc, container));
                sm.setSubject(getSubject(rc));
                sm.setTo(Arrays.asList(address));
                sm.setFrom(getFrom(rc));
                sm.setReplyTo(getReplyTo(rc));
                addAttachments(sm, rc);
                msgs.add(sm);
                log.info("sent email ok: " + address);
            } else {
                log.warn("no external email address for: " + user.getUrl());
            }
        }
        if (msgs.isEmpty()) {
            log.warn("No recipients (or none valid) in group");
        } else {
            IUser curUser = _(CurrentUserService.class).getSecurityContextUser();
            UUID curUserId = null;
            if (curUser != null) {
                curUserId = curUser.getNameNodeId();
            }
            RootContext rootContext = _(RootContextLocator.class).getRootContext();
            String href = rc.getTargetPage().getHref();
            _(RetryingMailService.class).sendMails(href, msgs, new NotifySenderEmailCallback(curUserId, rootContext));
        }
    }

    @Override
    public Path getPath() {
        return container.getPath().child(name);
    }

    private void addAttachments(StandardMessage sm, RenderContext rc) {
        Object o = EvalUtils.eval(attachments, rc, rc.getTargetPage());
        if (o == null) {
            return;
        } else if (o instanceof List) {
            addAttachments(sm, (List) o);
        } else if (o instanceof GetableResource) {
            addAttachment(sm, (GetableResource) o);
        } else if (o instanceof String) {
            String s = (String) o;
            if (s.trim().length() == 0) {
                return;
            } else {
                throw new RuntimeException("Unsupported attachment type: " + o.getClass() + " Should be reference to resource, or a list of resources");
            }
        } else {
            throw new RuntimeException("Unsupported attachment type: " + o.getClass() + " Should be reference to resource, or a list of resources");
        }
    }

    private void addAttachments(StandardMessage sm, List oList) {
        for (Object o : oList) {
            if (o == null) {
                // skip
            } else if (o instanceof GetableResource) {
                addAttachment(sm, (GetableResource) o);
            } else {
                throw new RuntimeException("Unsupported attachment type: " + o.getClass() + " Should be reference to resource, or a list of resources");
            }
        }
    }

    private void addAttachment(StandardMessage sm, GetableResource r) {
        try {
            BufferingOutputStream bufOut = new BufferingOutputStream(50000);
            r.sendContent(bufOut, null, null, null);
            bufOut.close();
            InputStream in = null;
            try {
                in = bufOut.getInputStream();
                sm.addAttachment(r.getName(), r.getContentType(null), null, in);
            } finally {
                IOUtils.closeQuietly(in);
            }
        } catch (NotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (NotAuthorizedException ex) {
            throw new RuntimeException(ex);
        } catch (BadRequestException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String toEmail(RetryingMailService.DelayMessage msg) {
        StringBuilder sb = new StringBuilder();
        for (MailboxAddress mbox : msg.getSm().getTo()) {
            sb.append(mbox.toPlainAddress()).append(";");
        }
        return sb.toString();
    }

    private String toStatus(RetryingMailService.DelayMessage msg) {
        if (msg.isCompletedOk()) {
            return "Sent";
        } else if (msg.isFatal()) {
            return "Failed";
        } else if (msg.getAttempts() > 0) {
            return "Retrying";
        } else {
            return "";
        }
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public Object authenticate(String user, String password) {
        return ((Templatable) getContainer()).authenticate(user, password);
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {
        return ((Templatable) getContainer()).authorise(request, method, auth);
    }

    @Override
    public String getRealm() {
        return ((Templatable) getContainer()).getRealm();
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
    public Object authenticate(DigestResponse digestRequest) {
        return ((DigestResource) getContainer()).authenticate(digestRequest);
    }

    @Override
    public boolean isDigestAllowed() {
        return ((DigestResource) getContainer()).isDigestAllowed();
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getContentType(String accepts) {
        return null;
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public Date getCreateDate() {
        return null;
    }

    private boolean isTestOnly(Map<String, String> parameters) {
        String test = parameters.get("test");
        if (test != null && test.length() > 0) {
            return !test.equals("false");
        } else {
            return false;
        }
    }

    private static class NotifySenderEmailCallback implements EmailResultCallback {

        private final UUID sender;
        private RootContext rootContext;
        private String fromAddress;

        public NotifySenderEmailCallback(UUID sender, RootContext rootContext) {
            this.sender = sender;
            this.rootContext = rootContext;
        }

        @Override
        public void onSuccess(StandardMessage sm) {
            log.info("Sent email ok: " + toRecipList(sm.getTo()));
        }

        @Override
        public void onFailed(StandardMessage sm, Throwable lastException) {
            log.info("Send email failed: " + toRecipList(sm.getTo()));
        }

        @Override
        public void finished(String id, final Collection<DelayMessage> msgs) {
            log.info("Finished mail job: " + id);
            rootContext.execute(new Executable2() {

                @Override
                public void execute(Context context) {
                    if (sender == null) {
                        return;
                    }
                    User user = (User) ClydeUtils.loadResource(sender);
                    if (user == null) {
                        log.warn("User not found: " + sender);
                        return;
                    } else {
                        String s = formatResultMessage(msgs);
                        List<String> to = Arrays.asList(user.getExternalEmailText());
                        if (fromAddress != null) {
                            _(MailSender.class).sendMail(fromAddress, null, to, fromAddress, "Group email notification", s);
                        }
                    }
                }

                private String formatResultMessage(Collection<DelayMessage> msgs) {
                    StringBuilder sb = new StringBuilder();
                    for (DelayMessage dm : msgs) {
                        sb.append(toRecipList(dm.getSm().getTo()));
                        sb.append(toEmailResult(dm));
                        sb.append("\n");
                    }
                    return sb.toString();
                }
            });
        }

        private String toRecipList(List<MailboxAddress> to) {
            StringBuilder sb = new StringBuilder();
            for (MailboxAddress mb : to) {
                sb.append(mb.toPlainAddress()).append(", ");
            }
            return sb.toString();
        }

        private String toEmailResult(DelayMessage dm) {
            if (dm.isCompletedOk()) {
                return "OK";
            } else {
                return "Failed after " + dm.getAttempts() + " attempts, last message: " + dm.getLastException();
            }

        }
    }

    public static class SendStatus {

        private String email;
        private int retries;
        private String status;

        public SendStatus(String email, int retries, String status) {
            this.email = email;
            this.retries = retries;
            this.status = status;
        }

        public String getEmail() {
            return email;
        }

        public int getRetries() {
            return retries;
        }

        public String getStatus() {
            return status;
        }
    }
}
