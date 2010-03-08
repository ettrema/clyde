package com.bradmcevoy.web.mail;

import com.bradmcevoy.web.ClydeStandardMessage;
import com.bradmcevoy.web.Folder;
import javax.mail.internet.MimeMessage;

/**
 *
 */
public interface MimeMessageParser {
    ClydeStandardMessage parseAndPersist(MimeMessage mm, Folder destFolder);
}
