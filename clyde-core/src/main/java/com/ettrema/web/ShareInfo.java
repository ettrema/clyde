package com.ettrema.web;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class ShareInfo {
	private final String userName;
	private final String userPath;
	private final List<String> roles = new ArrayList<String>();

	public ShareInfo(String userName, String userPath) {
		this.userName = userName;
		this.userPath = userPath;
	}

	public List<String> getRoles() {
		return roles;
	}

	public String getUser() {
		return userName;
	}

	public String getUserPath() {
		return userPath;
	}
	
}
