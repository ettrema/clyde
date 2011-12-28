package com.ettrema.web.security;

import com.ettrema.web.security.PermissionRecipient.Role;

/**
 *
 * @author brad
 */
public class SimplePermission {
	private final String subjectName;
	private final Role role;

	public SimplePermission(String subjectName, Role role) {
		this.subjectName = subjectName;
		this.role = role;
	}

	public Role getRole() {
		return role;
	}

	public String getSubjectName() {
		return subjectName;
	}
	
	
}
