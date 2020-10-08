package acs.logic.implementation;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import acs.data.Converter;
import acs.data.ElementEntity;
import acs.logic.ElementService;
import acs.logic.ObjectNotFoundException;
import acs.rest.boundaries.element.ElementBoundary;
import acs.rest.boundaries.element.ElementIdBoundary;
import acs.rest.boundaries.user.UserIdBoundary;

//@Service
public class ElementServiceImplementation implements ElementService {
	private Map<String, ElementEntity> elementDatabase;
	private Converter converter;
	private String projectName;

	@Autowired
	public ElementServiceImplementation(Converter converter) {
		this.converter = converter;
	}

	// injection of project name from the spring boot configuration
	@Value("${spring.application.name: generic}")
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	@PostConstruct
	public void init() {
		// since this class is a singleton, we generate a thread safe collection
		this.elementDatabase = Collections.synchronizedMap(new TreeMap<>());
	}

	@Override
	public ElementBoundary create(String managerDomain, String managerEmail, ElementBoundary elementDetails) {
		elementDetails.setElementId(new ElementIdBoundary(projectName, UUID.randomUUID().toString()));
		ElementEntity entity = this.converter.toEntity(elementDetails);
		entity.setTimeStamp(new Date());
		Map<String, UserIdBoundary> createdBy = new HashMap<>();
		createdBy.put("userId", new UserIdBoundary(managerDomain, managerEmail));
		entity.setCreateBy(createdBy);
		this.elementDatabase.put(entity.getElementId().toString(), entity);
		return this.converter.fromEntity(entity);
	}

	@Override
	public ElementBoundary update(String managerDomain, String managerEmail, String elementDomain, String elementId,
			ElementBoundary update) {
		ElementEntity existing = this.elementDatabase.get(elementDomain + "#" + elementId);
		if (existing == null)
			throw new ObjectNotFoundException("could not find object by id:" + elementId);
		else {
			if (update.getActive() != null)
				existing.setActive(update.getActive());
			if (update.getName() != null)
				existing.setName(update.getName());
			if (update.getLocation() != null)
				existing.setLocation(update.getLocation());
			if (update.getType() != null)
				existing.setType(update.getType());
			return this.converter.fromEntity(existing);
		}
	}

	@Override
	public List<ElementBoundary> getAll(String userDomain, String userEmail) {
		if (userDomain != null && !userDomain.trim().isEmpty() && userEmail != null && !userEmail.trim().isEmpty()) {
			return this.elementDatabase.values().stream().map(this.converter::fromEntity).collect(Collectors.toList());
		} else {
			throw new RuntimeException("User Domain and User Email must not be empty or null");
		}
	}

	@Override
	public ElementBoundary getSpecificElement(String userDomain, String userEmail, String elementDomain,
			String elementId) {
		ElementEntity existing = this.elementDatabase.get(elementDomain + "#" + elementId);
		if (existing != null) {
			return this.converter.fromEntity(existing);
		} else {
			throw new ObjectNotFoundException("could not find object by id: " + elementId);
		}

	}

	@Override
	public void deleteAllElements(String adminDomain, String adminEmail) {
		if (adminDomain != null && !adminDomain.trim().isEmpty() && adminEmail != null
				&& !adminEmail.trim().isEmpty()) {
			this.elementDatabase.clear();
		} else {
			throw new RuntimeException("Admin Domain and Admin Email must not be empty or null");
		}
	}

}
