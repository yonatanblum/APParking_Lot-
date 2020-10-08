package acs.init;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import acs.data.TypeEnum;
import acs.logic.ActionService;
import acs.rest.boundaries.action.ActionBoundary;
import acs.rest.boundaries.action.ActionIdBoundary;
import acs.rest.boundaries.action.ElementOfAction;
import acs.rest.boundaries.action.InvokingUser;
import acs.rest.boundaries.element.ElementIdBoundary;
import acs.rest.boundaries.user.UserIdBoundary;

@Component
@Profile("production")
public class ActionsCreator implements CommandLineRunner {
	private ActionService actionService;
	ObjectMapper mapper;

	public ActionsCreator() {
		this.mapper = new ObjectMapper();
	}

	@Autowired
	public ActionsCreator(ActionService actionService) {
		this.actionService = actionService;
	}

	@Override
	public void run(String... args) throws Exception {
//		Map<String, Object> element = new HashMap<String, Object>();
		InvokingUser invokedBy = new InvokingUser();
		Map<String, Object> actionAttributes = new HashMap<String, Object>();
		ElementOfAction element = new ElementOfAction(new ElementIdBoundary("tamir", null));
//		element.put("domain", "tamir");
//		element.put("id", "6464");

		invokedBy.setUserId(new UserIdBoundary("2020b", "t@gmail.com"));

		actionAttributes.put("key1", "value1");
		actionAttributes.put("key2", "value2");
		actionAttributes.put("key3", "value3");

		ActionBoundary actionBoundary = new ActionBoundary(new ActionIdBoundary("tamir", null),
				TypeEnum.actionType.name(), element, new Date(), invokedBy, actionAttributes);
		
		IntStream.range(1, 6).mapToObj(i -> {
			actionBoundary.setActionId(new ActionIdBoundary("tamir", Integer.toString(i)));
			return actionBoundary;
		}).forEach(System.err::println);

		this.actionService.invokeAction(actionBoundary);

	}
}
