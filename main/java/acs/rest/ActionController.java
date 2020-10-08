package acs.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import acs.logic.ActionService;
import acs.rest.boundaries.action.ActionBoundary;

@RestController
public class ActionController {
	private ActionService actionService;

	public ActionController(ActionService actionService) {
		super();
		this.actionService = actionService;

	}

	@Autowired
	public ActionController() {

	}

	@Autowired
	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	// This method need to return any object relate to the action...
	@RequestMapping(path = "/acs/actions", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public Object invokeAnAction(@RequestBody ActionBoundary actionDetails) {
		return actionService.invokeAction(actionDetails);

	}
}
