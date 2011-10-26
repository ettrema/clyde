package com.ettrema.web.mail;

import com.ettrema.web.ClydeStandardMessage;
import com.ettrema.web.Folder;
import com.ettrema.web.User;
import com.ettrema.context.RequestContext;
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
