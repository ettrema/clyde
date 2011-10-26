package com.ettrema.web.mail;

import com.ettrema.mail.StandardMessage;

/**
 * Modifies the message object given the original content of that message and the dataObject
 * 
 * The dataObject may represent a user, etc
 *
 * The msg may be a StandardMessageWrapper
 *
 */
public interface Templater {
    void doTemplating(StandardMessage msg, Object dataObject);
}
