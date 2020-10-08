package acs.logic;

import java.util.List;

import acs.rest.boundaries.user.UserBoundary;

public interface EnhancedUserService extends UserService {
	
	public List<UserBoundary> getAllUsers(String adminDomain , String adminEmail,int size,int page);

}
