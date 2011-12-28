package com.ettrema.web.groups;

import com.bradmcevoy.http.webdav.WebDavProtocol;
import com.ettrema.web.IUser;
import com.ettrema.web.security.Subject;
import com.ettrema.web.security.SystemUserGroup;
import javax.xml.namespace.QName;

/**
 *
 * @author brad
 */
public class AuthenticatedUserGroup implements SystemUserGroup {

	@Override
    public String getSubjectName() {
        return "Authenticated";
    }

	@Override
    public boolean isInGroup( Subject user ) {
        if( user instanceof IUser ) {
            return user != null;
        } else {
            return false;
        }
    }

	@Override
    public boolean isOrContains(Subject s) {
        return isInGroup(s);
    }

	@Override
	public PrincipleId getIdenitifer() {
		return new PrincipleId() {

			@Override
			public QName getIdType() {
				return new QName(WebDavProtocol.DAV_URI, "all", WebDavProtocol.DAV_PREFIX );
			}

			@Override
			public String getValue() {
				return null;
			}
		};
	}


}
