package com.ettrema.web.mail;

import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
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

    Folder getMailFolder(User user, String name, boolean create) throws ConflictException, NotAuthorizedException, BadRequestException;
}
