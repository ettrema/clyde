package com.bradmcevoy.web.component;

import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Namespace;
import java.util.Arrays;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.Group;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.io.BufferingOutputStream;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.security.UserGroup;
import com.ettrema.mail.MailServer;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.eval.EvalUtils;
import com.bradmcevoy.web.eval.Evaluatable;
import com.bradmcevoy.web.groups.GroupService;
import com.ettrema.mail.MailboxAddress;
import com.ettrema.mail.StandardMessage;
import com.ettrema.mail.StandardMessageImpl;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import org.apache.commons.io.IOUtils;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;

/**
 * An email command which sends to a group, rather then a particular user
 *
 * @author brad
 */
public final class GroupEmailCommand2 extends Command {

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
        Group group = getGroup(rc);
        return group.getMembers();
    }

    private MailboxAddress getAddress(User user) {
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

    private Group getGroup(RenderContext rc) {
        Object o = EvalUtils.eval(toGroup, rc, container);
        if (o == null) {
            throw new RuntimeException("Expression returned null, should have returned group or name of group");
        } else if (o instanceof String) {
            GroupService groupService = _(GroupService.class);
            String groupName = (String) o;
            UserGroup group = groupService.getGroup((Resource) this.getContainer(), groupName);
            if (group == null) {
                throw new RuntimeException("Unknown group: " + groupName);
            }
            if (group instanceof Group) {
                Group g = (Group) group;
                return g;
            } else {
                throw new RuntimeException("Group " + groupName + " is not an appropriate type. Is a: " + group.getClass() + " - but must be a: " + Group.class);
            }

        } else if (o instanceof Group) {
            return (Group) o;
        } else {
            throw new RuntimeException("Un-supported group type: " + o.getClass().getName());
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
    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
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
        return doProcess(rc, parameters, files);
    }

    @Override
    protected String doProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        log.trace("doProcess");
        try {
            send(rc);
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

    public void send(RenderContext rc) throws MessagingException {
        log.debug("send");
        List<User> recipList = getTo(rc);
        MailServer mailServer = requestContext().get(MailServer.class);
        boolean didSend = false;
        for (User user : recipList) {
            MailboxAddress address = getAddress(user);
            if (address != null) {
                log.info("send email to: " + address + " ...");
                StandardMessage sm = new StandardMessageImpl();
                sm.setText(EvalUtils.evalToString(bodyText, rc, container));
                sm.setHtml(EvalUtils.evalToString(bodyHtml, rc, container));
                sm.setSubject(getSubject(rc));
                sm.setTo(Arrays.asList(address));
                sm.setFrom(getFrom(rc));
                sm.setReplyTo(getReplyTo(rc));
                addAttachments(sm, rc);
                mailServer.getMailSender().sendMail(sm);
                didSend = true;
                log.info("sent email ok: " + address);
            } else {
                log.warn("no external email address for: " + user.getUrl());
            }
        }
        if (!didSend) {
            Group group = getGroup(rc);
            log.warn("No recipients (or none valid) in group: " + group.getHref());
        }
    }

    @Override
    public Path getPath() {
        return container.getPath().child(name);
    }

    private void addAttachments(StandardMessage sm, RenderContext rc) {
        Object o = EvalUtils.eval(attachments, rc, container);
        if (o == null) {
            return;
        } else if (o instanceof List) {
            addAttachments(sm, (List) o);
        } else if (o instanceof GetableResource) {
            addAttachment(sm, (GetableResource) o);
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
            r.sendContent( bufOut, null, null, null);
            bufOut.close();
            InputStream in = null;
            try {
                in = bufOut.getInputStream();
                sm.addAttachment(r.getName(), r.getContentType(null), null, in);
            } finally {
                IOUtils.closeQuietly(in);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (NotAuthorizedException ex) {
            throw new RuntimeException(ex);
        } catch (BadRequestException ex) {
            throw new RuntimeException(ex);
        }
    }
}
