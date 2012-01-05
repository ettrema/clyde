package com.ettrema.web.security;

import com.ettrema.web.Component;
import com.ettrema.web.component.ForgottenPasswordComponent;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.RenderContext;
import com.ettrema.web.RequestParams;
import com.ettrema.web.Templatable;
import com.ettrema.web.User;
import com.ettrema.context.RequestContext;
import com.ettrema.mail.MailboxAddress;
import com.ettrema.mail.StandardMessageImpl;
import com.ettrema.mail.send.MailSender;
import com.ettrema.vfs.VfsSession;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.mvel.TemplateInterpreter;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class ForgottenPasswordHelper {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ForgottenPasswordHelper.class);
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_TOKEN = "token";
    private MailboxAddress parsedEmail;
    private String token;
    private String password; // the requested new password
    private User foundUser; // the located user

    public String onProcess(ForgottenPasswordComponent comp, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) throws NotAuthorizedException {
        log.debug("onProcess");

        if (isResetRequest(parameters)) {
            log.trace("is reset request");
            return processReset(comp, rc, parameters);
        } else {
            log.trace("send email");
            return sendResetEmail(comp, rc, parameters, files);
        }
    }

    private String sendResetEmail(ForgottenPasswordComponent comp, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) throws IllegalArgumentException, NullPointerException {
        if (!validateEmailRequest(comp, rc, parameters)) {
            log.trace("password reset request was not valid");
            return null;
        }
        log.trace("password reset request is valid");
        MailSender sender = RequestContext.getCurrent().get(MailSender.class);
        MailboxAddress to = parsedEmail;
        token = _(PasswordRecoveryService.class).createToken(foundUser);
        String currentPassword = _(PasswordStorageService.class).getPasswordValue(foundUser);

        Templatable target = rc.getTargetPage();
        String text = evalTemplate(target, comp.getBodyTemplate(), foundUser, token, currentPassword, parsedEmail.toPlainAddress());
        String html = evalTemplate(target, comp.getBodyTemplateHtml(), foundUser, token, currentPassword, parsedEmail.toPlainAddress());
        String rt = (comp.getReplyTo() == null) ? comp.getFromAdd() : comp.getReplyTo();
        if (StringUtils.isEmpty(comp.getFromAdd())) {
            throw new IllegalArgumentException("from address is null");
        }
        if (rt == null) {
            throw new IllegalArgumentException("no reply to or from address");
        }
        StandardMessageImpl sm = new StandardMessageImpl();
        MailboxAddress from = MailboxAddress.parse(comp.getFromAdd());
        sm.setFrom(from);
        MailboxAddress replyTo = MailboxAddress.parse(rt);
        sm.setReplyTo(replyTo);
        sm.setTo(Arrays.asList(to));
        sm.setSubject(comp.getSubject());
        if (!StringUtils.isEmpty(text)) {
            sm.setText(text);
        }
        if (!StringUtils.isEmpty(html)) {
            sm.setHtml(html);
        }
        sender.sendMail(sm);
        RequestParams.current().getAttributes().put(comp.getName() + "_confirmed", Boolean.TRUE); // deprecated
        RequestParams.current().getAttributes().put(comp.getName() + "Confirmed", Boolean.TRUE);
        return checkRedirect(comp.getThankyouPage());
    }

    private String evalTemplate(Templatable targetPage, String template, User user, String token, String password, String email) {
        if (template == null) {
            return null;
        }
        try {
            Map map = new HashMap();
            map.put("user", user);
            map.put("email", email);
            map.put("token", token);
            map.put("password", password);
            map.put("targetPage", targetPage);
            String s = TemplateInterpreter.evalToString(template, map);
            return s;
        } catch (Throwable e) {
            log.error("Exception rendering template: " + template, e);
            return "ERR";
        }

    }

    /**
     * Check that the request to send a reset email is valid
     *
     * @param rc
     * @return
     */
    private boolean validateEmailRequest(ForgottenPasswordComponent comp, RenderContext rc, Map<String, String> parameters) {
        // Look for a user matching the given email address. If none is found a validation message is set
        findUser(comp, parameters);
        if (foundUser != null) {
            if (StringUtils.isNotBlank(comp.getRecaptchaComponent())) {
                log.trace("requires captcha");
                Component c = rc.page.getComponent(comp.getRecaptchaComponent(), false);
                if (c == null) {
                    throw new RuntimeException("Recaptcha component not found: " + comp.getRecaptchaComponent());
                }
                if (!c.validate(rc)) {
                    log.trace("recaptcha validation failed");
                    return false;
                }
            }


            rc.addAttribute(comp.getName() + "_found", foundUser);
            return true;
        } else {
            return false;
        }
    }

    private void findUser(ForgottenPasswordComponent comp, Map<String, String> parameters) {
        String email = parameters.get("email");
        try {
            // Parsing validates the email
            MailboxAddress add = MailboxAddress.parse(email);
            this.parsedEmail = add;
            foundUser = _(EmailAuthenticator.class).getUserLocator().findUserByEmail(add, (CommonTemplated) comp.getContainer());

            if (foundUser != null) {
                log.debug("found user: " + foundUser.getName());
            } else {
                comp.setValidationMessage("We could not find your email address. Please try again or contact the site administrator");
                log.debug("no users found");
            }
        } catch (IllegalArgumentException e) {
            log.debug("invalid email address: error: " + email);
            comp.setValidationMessage("Please check the format of your email address, it should read like ben@somewhere.com");
        }
    }

    /**
     * Check that the request to perform the password reset is valid
     *
     * @param rc
     * @return
     */
    private boolean validateResetRequest(ForgottenPasswordComponent comp, RenderContext rc, Map<String, String> parameters) {
        token = parameters.get(PARAM_TOKEN);
        findUser(comp, parameters);
        if (foundUser == null) {
            comp.setValidationMessage("No user found");
            log.trace("user not found");
            return false;
        }
        if (!_(PasswordRecoveryService.class).isTokenValid(token, foundUser)) {
            log.trace("token is not valid");
            comp.setValidationMessage("The requested token is not valid");
            return false;
        }
        this.password = parameters.get(PARAM_PASSWORD);

        String passwordErr = _(PasswordValidationService.class).checkValidity(foundUser, password);
        if (passwordErr != null) {
            comp.setValidationMessage(passwordErr);
            return false;
        }
        log.trace("reset request is valid");
        return true;
    }

    /**
     * Determine if the request is for sending a reset email, or for actually
     * performing the reset
     *
     * @param parameters
     * @return - true if the request is to perform the actual reset of the user's password
     */
    private boolean isResetRequest(Map<String, String> parameters) {
        return parameters.containsKey(PARAM_TOKEN);
    }

    /**
     * This is the second phase, when the user submits a new password
     *
     * @param comp
     * @param rc
     * @param parameters
     * @return
     */
    private String processReset(ForgottenPasswordComponent comp, RenderContext rc, Map<String, String> parameters) {
        if (!validateResetRequest(comp, rc, parameters)) {
            return null;
        }
        log.trace("all ok, update password");
        foundUser.setPassword(password);
        foundUser.save();
        log.trace("saved, now commit");
        _(VfsSession.class).commit();
        RequestParams.current().getAttributes().put(comp.getName() + "Changed", Boolean.TRUE);
        return checkRedirect(comp.getThankyouPage());
    }

    private String checkRedirect(String thankyouPage) {
        if (StringUtils.isNotBlank(thankyouPage)) {
            return thankyouPage;
        } else {
            return null;
        }
    }
}
