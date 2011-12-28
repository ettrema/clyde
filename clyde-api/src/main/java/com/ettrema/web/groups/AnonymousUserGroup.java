package com.ettrema.web.groups;

import com.bradmcevoy.http.webdav.WebDavProtocol;
import com.ettrema.web.security.Subject;
import com.ettrema.web.security.SystemUserGroup;
import javax.xml.namespace.QName;

/**
 * Represents anyone or anything, regardless of whether or not they are logged in
 * (which means the name is a bit of a mis-nomer, it doesnt *only* apply to
 * anonymous users)
 *
 * @author brad
 */
public class AnonymousUserGroup implements SystemUserGroup {

	@Override
    public String getSubjectName() {
        return "Anonymous";
    }

	@Override
    public boolean isInGroup( Subject user ) {
        return true;
    }

	@Override
    public boolean isOrContains(Subject s) {
        return true;
    }

	@Override
	public PrincipleId getIdenitifer() {
		return new PrincipleId() {

			@Override
			public QName getIdType() {
				return new QName(WebDavProtocol.DAV_URI, "anonymous", WebDavProtocol.DAV_PREFIX );
			}

			@Override
			public String getValue() {
				return null;
			}
		};
	}



}
