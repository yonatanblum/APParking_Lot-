package acs.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import acs.logic.EnhancedUserService;
import acs.rest.boundaries.user.NewUserDetailsBoundary;
import acs.rest.boundaries.user.UserBoundary;
import acs.rest.boundaries.user.UserIdBoundary;

@RestController
public class UserController {
	private EnhancedUserService userService;

	@Autowired
	public UserController() {
	}

	@Autowired
	public void setUserService(EnhancedUserService userService) {
		this.userService = userService;
	}

	@RequestMapping(path = "/acs/users/login/{userDomain}/{userEmail}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary loginValidUser(@PathVariable("userDomain") String userDomain,
			@PathVariable("userEmail") String userEmail) {
		return this.userService.login(userDomain, userEmail);

	}

	@RequestMapping(path = "/acs/users", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary CreateNewUser(@RequestBody NewUserDetailsBoundary userDetails) {

		return this.userService.createUser(new UserBoundary(new UserIdBoundary("", userDetails.getEmail()),
				userDetails.getRole(), userDetails.getUsername(), userDetails.getAvatar()));
	}

	@RequestMapping(path = "/acs/users/{userDomain}/{userEmail}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void updateUserDetails(@PathVariable("userDomain") String userDomain,
			@PathVariable("userEmail") String userEmail, @RequestBody UserBoundary update) {
		userService.updateUser(userDomain, userEmail, update);
	}

}
