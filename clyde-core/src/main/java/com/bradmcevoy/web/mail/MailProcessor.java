package com.bradmcevoy.web.mail;

import com.bradmcevoy.context.RequestContext;
import com.bradmcevoy.web.ClydeStandardMessage;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.User;
import com.ettrema.mail.MailboxAddress;
import java.util.List;
import javax.mail.internet.MimeMessage;

/**
 *
 */
public interface MailProcessor {

    void handleGroupEmail(MimeMessage mm, Folder destFolder, RequestContext context, List<User> members, MailboxAddress groupAddress, String discardSubjects);

    void forwardEmail(MimeMessage mm, String emailRecip, RequestContext context);

    ClydeStandardMessage persistEmail(MimeMessage mm, Folder destFolder, RequestContext context) throws RuntimeException;

}
