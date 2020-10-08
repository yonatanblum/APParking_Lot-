package acs.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import acs.logic.EnhancedActionService;
import acs.logic.EnhancedElementService;
import acs.logic.EnhancedUserService;
import acs.rest.boundaries.action.ActionBoundary;
import acs.rest.boundaries.user.UserBoundary;

@RestController
public class AdminController {

	private EnhancedUserService userService;
	private EnhancedActionService actionService;
	private EnhancedElementService elementService;

	@Autowired
	public AdminController() {
	}

	@Autowired
	public void setElementService(EnhancedElementService elementService) {
		this.elementService = elementService;
	}

	@Autowired
	public void setUserService(EnhancedUserService userService) {
		this.userService = userService;
	}

	@Autowired
	public void setActionService(EnhancedActionService actionService) {
		this.actionService = actionService;
	}

	@RequestMapping(path = "/acs/admin/users/{adminDomain}/{adminEmail}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary[] exportAllUsers(@PathVariable("adminDomain") String adminDomain,
			@PathVariable("adminEmail") String adminEmail,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		return this.userService.getAllUsers(adminDomain, adminEmail, size, page).toArray(new UserBoundary[0]);

	}

	@RequestMapping(path = "/acs/admin/actions/{adminDomain}/{adminEmail}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ActionBoundary[] exportAllActions(@PathVariable("adminDomain") String adminDomain,
			@PathVariable("adminEmail") String adminEmail,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		return actionService.getAllActions(adminDomain, adminEmail, size, page).toArray(new ActionBoundary[0]);
	}

	@RequestMapping(path = "/acs/admin/users/{adminDomain}/{adminEmail}", method = RequestMethod.DELETE)
	public void deleteAllUsers(@PathVariable("adminDomain") String adminDomain,
			@PathVariable("adminEmail") String adminEmail) {
		userService.deleteAllUsers(adminDomain, adminEmail);
	}

	@RequestMapping(path = "/acs/admin/elements/{adminDomain}/{adminEmail}", method = RequestMethod.DELETE)
	public void deleteAllElements(@PathVariable("adminDomain") String adminDomain,
			@PathVariable("adminEmail") String adminEmail) {
		elementService.deleteAllElements(adminDomain, adminEmail);

	}

	@RequestMapping(path = "/acs/admin/actions/{adminDomain}/{adminEmail}", method = RequestMethod.DELETE)
	public void deleteAllActions(@PathVariable("adminDomain") String adminDomain,
			@PathVariable("adminEmail") String adminEmail) {
		actionService.deleteAllActions(adminDomain, adminEmail);
	}
}
