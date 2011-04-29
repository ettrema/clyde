package com.bradmcevoy.web.manage.logging;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.utils.CurrentRequestService;
import com.bradmcevoy.web.IUser;
import com.bradmcevoy.web.security.CurrentUserService;
import com.ettrema.berry.event.Notifier;
import com.ettrema.common.Service;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author brad
 */
public class WebErrorReporter implements Service, com.ettrema.logging.NotifyingAppender.Listener {

    private final Notifier notifier;
    private final CurrentRequestService currentRequestService;
    private final CurrentUserService currentUserService;
    private int urgency = 2;

    public WebErrorReporter(Notifier notifier, CurrentRequestService currentRequestService, CurrentUserService currentUserService) {
        this.notifier = notifier;
        this.currentRequestService = currentRequestService;
        this.currentUserService = currentUserService;
    }

    public int getUrgency() {
        return urgency;
    }

    public void setUrgency(int i) {
        this.urgency = i;
    }

    public void start() {
        com.ettrema.logging.NotifyingAppender.addListener(this);
    }

    public void stop() {
        com.ettrema.logging.NotifyingAppender.removeListener(this);
    }

    public void onEvent(org.apache.log4j.spi.LoggingEvent event) {
        if (event.getLevel() == Level.ERROR) {
            System.out.println("report it");
            String errorText = formatException(event);
            errorText += "------------------\n\n";
            errorText += formatRequest();
            errorText += "------------------\n\n";
            errorText += formatCurrentUser();
            notifier.notify(2, event.getLocationInformation().getClassName(), errorText);
        }
    }

    private String formatException(LoggingEvent event) {
        if (event.getThrowableInformation() != null) {
            String s = event.getMessage() + "\n";
            for (String n : event.getThrowableInformation().getThrowableStrRep()) {
                s += n + "\n";
            }
            return s;
        } else {
            return event.getMessage() + "";
        }
    }

    private String formatRequest() {
        Request r = currentRequestService.request();
        if (r == null) {
            return "No HTTP request\n";
        } else {
            String s = r.getAbsoluteUrl() + "\n";
            s += r.getMethod() + "\n";
            if (r.getParams() != null) {
                for (String paramName : r.getParams().keySet()) {
                    String val = r.getParams().get(paramName);
                    s += paramName + "=" + val + "\n";
                }
            }
            return s;
        }
    }

    private String formatCurrentUser() {
        IUser user = currentUserService.getSecurityContextUser();
        String s = "Security context user: ";
        if (user != null) {
            s += user.getHref() + "\n";
        } else {
            s += "none\n";
        }
        user = currentUserService.getOnBehalfOf();
        if (user != null) {
            s += "On behalf of user: " + user.getHref();
        } else {
            s += "none";
        }
        return s;
    }
}
