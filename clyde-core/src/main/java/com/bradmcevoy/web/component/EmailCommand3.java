package com.bradmcevoy.web.component;

import org.jdom.Namespace;
import java.util.Arrays;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.web.User;
import com.ettrema.mail.MailServer;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.eval.EvalUtils;
import com.bradmcevoy.web.eval.Evaluatable;
import com.ettrema.mail.MailboxAddress;
import com.ettrema.mail.StandardMessage;
import com.ettrema.mail.StandardMessageImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import org.jdom.Element;

/**
 * Intended for sending to single email recipients, but will cope if the expression
 * evaluates to a list
 *
 * The expression must be a string represetion of an email address of a User object,
 * or a list of either.
 *
 * @author brad
 */
public final class EmailCommand3 extends Command {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EmailCommand.class);
    private static final long serialVersionUID = 1L;
    public static final Namespace NS = Namespace.getNamespace("c", "http://clyde.ettrema.com/ns/core");
    private Evaluatable bodyText;
    private Evaluatable bodyHtml;
    private Evaluatable from;
    private Evaluatable to;
    private Evaluatable subject;
    private Evaluatable replyTo;
    private Evaluatable confirmationUrl;

    public EmailCommand3(Addressable container, String name) {
        super(container, name);
    }

    public EmailCommand3(Addressable container, Element el) {
        super(container, el);
        parseXml(el);
    }

    public void parseXml(Element el) {
        bodyText = EvalUtils.getEval(el, "bodyText", NS);
        bodyHtml = EvalUtils.getEval(el, "bodyHtml", NS);
        from = EvalUtils.getEval(el, "from", NS);
        to = EvalUtils.getEval(el, "to", NS);
        subject = EvalUtils.getEval(el, "subject", NS);
        replyTo = EvalUtils.getEval(el, "replyTo", NS);
        confirmationUrl = EvalUtils.getEval(el, "confirmationUrl", NS);
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
        EvalUtils.setEval(e2, "to", to, NS);
        EvalUtils.setEval(e2, "subject", subject, NS);
        EvalUtils.setEval(e2, "replyTo", replyTo, NS);
        EvalUtils.setEval(e2, "confirmationUrl", confirmationUrl, NS);

    }

    @Override
    public String renderEdit(RenderContext rc) {
        return "todo";
    }

    private MailboxAddress getFrom(RenderContext rc) {
        String s = EvalUtils.evalToString(from, rc, container);
        return MailboxAddress.parse(s);
    }

    private List<MailboxAddress> getTo(RenderContext rc) {
        Object o = EvalUtils.eval(to, rc, rc.getTargetPage());
        List<MailboxAddress> list = new ArrayList<MailboxAddress>();
        if (o == null) {
            throw new RuntimeException("Expression returned null, should have returned an email address or list of users");
        } else if (o instanceof List) {
            for (Object oRecip : (List) o) {
                list.add(getAddress(oRecip));
            }
        } else {
            MailboxAddress add = getAddress(o);
            list.add(add);
        }
        return list;
    }

    private MailboxAddress getAddress(Object o) {
        if (o instanceof String) {
            String s = (String) o;
            MailboxAddress add = MailboxAddress.parse(s);
            return add;
        } else if (o instanceof User) {
            return getAddress((User) o);
        } else {
            throw new RuntimeException("Un-supported recipient type: " + o.getClass().getName());
        }

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
        List<MailboxAddress> recipList = getTo(rc);
        MailServer mailServer = requestContext().get(MailServer.class);
        for (MailboxAddress address : recipList) {
            if (address != null) {

                StandardMessage sm = new StandardMessageImpl();
                sm.setText(EvalUtils.evalToString(bodyText, rc, container));
                sm.setHtml(EvalUtils.evalToString(bodyHtml, rc, container));
                sm.setSubject(getSubject(rc));
                sm.setTo(Arrays.asList(address));
                sm.setFrom(getFrom(rc));
                sm.setReplyTo(getReplyTo(rc));
                mailServer.getMailSender().sendMail(sm);
            } else {
                log.warn("null email address in to list");
            }
        }
    }

    @Override
    public Path getPath() {
        return container.getPath().child(name);
    }
}
