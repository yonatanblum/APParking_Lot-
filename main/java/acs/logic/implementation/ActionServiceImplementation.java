package acs.logic.implementation;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import acs.data.ActionEntity;
import acs.data.Converter;
import acs.logic.ActionService;
import acs.rest.boundaries.action.ActionBoundary;

//@Service
public class ActionServiceImplementation implements ActionService {

	private String projectName;
	private Map<String, ActionEntity> actionDatabase;
	private Converter converter;

	@Autowired
	public ActionServiceImplementation(Converter converter) {
		this.converter = converter;
	}

	// injection of project name from the spring boot configuration
	@Value("${spring.application.name: generic}")
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	@PostConstruct
	public void init() {
		this.actionDatabase = Collections.synchronizedMap(new TreeMap<>());
	}

	@Override
	public Object invokeAction(ActionBoundary action) {
		if (action == null) {
			throw new RuntimeException("ActionBoundary received in invokeAction method can't be null\n");
		} else {
			action.setCreatedTimestamp(new Date());
			action.getActionId().setDomain(projectName);
			action.getActionId().setId(UUID.randomUUID().toString());
			ActionEntity entity = converter.toEntity(action);
			actionDatabase.put(action.getActionId().toString(), entity);
			return action;
		}
	}

	@Override
	public List<ActionBoundary> getAllActions(String adminDomain, String adminEmail) {
		if (adminDomain != null && !adminDomain.trim().isEmpty() && adminEmail != null
				&& !adminEmail.trim().isEmpty()) {
			return this.actionDatabase.values().stream().map(this.converter::fromEntity).collect(Collectors.toList());
		} else {
			throw new RuntimeException("Admin Domain and Admin Email must not be empty or null");
		}

	}

	@Override
	public void deleteAllActions(String adminDomain, String adminEmail) {
		if (adminDomain != null && !adminDomain.trim().isEmpty() && adminEmail != null
				&& !adminEmail.trim().isEmpty()) {
			this.actionDatabase.clear();
		} else {
			throw new RuntimeException("Admin Domain and Admin Email must not be empty or null");
		}

	}

}
