package acs.rest.boundaries.action;

import acs.rest.boundaries.user.UserIdBoundary;

public class InvokingUser {
	private UserIdBoundary userId;

	public InvokingUser(UserIdBoundary userId) {
		this.userId = userId;
	}
	
	public InvokingUser() {
	}
	
	public UserIdBoundary getUserId() {
		return userId;
	}

	public void setUserId(UserIdBoundary userId) {
		this.userId = userId;
	}	
}
