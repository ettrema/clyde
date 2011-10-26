package com.ettrema.web.mail;

import com.ettrema.web.ClydeStandardMessage;
import com.ettrema.web.Folder;
import javax.mail.internet.MimeMessage;

/**
 *
 */
public interface MimeMessageParser {
    ClydeStandardMessage parseAndPersist(MimeMessage mm, Folder destFolder);
}
