package acs.logic.implementation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import acs.aop.MyLogger;
import acs.aop.PerformanceMeasuring;
import acs.aop.PerformenceUnits;
import acs.dal.ElementDao;
import acs.dal.UserDao;
import acs.data.Converter;
import acs.data.ElementEntity;
import acs.data.ElementIdEntity;
import acs.data.UserEntity;
import acs.data.UserIdEntity;
import acs.data.UserRoleEntityEnum;
import acs.logic.EnhancedElementService;
import acs.logic.ObjectNotFoundException;
import acs.logic.ServiceTools;
import acs.rest.boundaries.element.ElementBoundary;
import acs.rest.boundaries.element.ElementIdBoundary;
import acs.rest.boundaries.user.UserIdBoundary;

@Service
public class DbElementServiceImplementation implements EnhancedElementService {
	private ElementDao elementDao;
	private Converter converter;
	private String projectName;
	private UserDao userDao;

	@Autowired
	public DbElementServiceImplementation(ElementDao elementDao, Converter converter, UserDao userDao) {
		this.elementDao = elementDao;
		this.converter = converter;
		this.userDao = userDao;
	}

	// injection of project name from the spring boot configuration
	@Value("${spring.application.name: generic}")
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	@Override
	@Transactional
	@MyLogger
	@PerformanceMeasuring(units = PerformenceUnits.ns)
	public ElementBoundary create(String managerDomain, String managerEmail, ElementBoundary elementDetails) {

		ServiceTools.stringValidation(managerDomain, managerEmail);

		UserIdEntity uib = new UserIdEntity(managerDomain, managerEmail);
		UserEntity existing = this.userDao.findById(uib).orElseThrow(() -> new ObjectNotFoundException(
				"could not find object by UserDomain:" + managerDomain + "or userEmail:" + managerEmail));

		if (!existing.getRole().equals(UserRoleEntityEnum.manager))
			throw new ObjectNotFoundException("You are not manager! Can't create an element");

		elementDetails.setElementId(new ElementIdBoundary(projectName, UUID.randomUUID().toString()));
		ElementEntity entity = this.converter.toEntity(elementDetails);
		entity.setTimeStamp(new Date());
		Map<String, UserIdBoundary> createdBy = new HashMap<>();
		createdBy.put("userId", new UserIdBoundary(managerDomain, managerEmail));
		entity.setCreateBy(createdBy);
		return this.converter.fromEntity(this.elementDao.save(entity));

	}

	@Override
	@Transactional
	@PerformanceMeasuring(units = PerformenceUnits.ns)
	public ElementBoundary update(String managerDomain, String managerEmail, String elementDomain, String elementId,
			ElementBoundary update) {

		ServiceTools.stringValidation(managerDomain, managerEmail, elementDomain, elementId);

		UserIdEntity uib = new UserIdEntity(managerDomain, managerEmail);

		UserEntity existingUser = this.userDao.findById(uib).orElseThrow(() -> new ObjectNotFoundException(
				"could not find object by UserDomain:" + managerDomain + "or userEmail:" + managerEmail));

		if (!existingUser.getRole().equals(UserRoleEntityEnum.manager))
			throw new ObjectNotFoundException("You are not manager! Can't update an element");

		ElementIdEntity elementIdEntity = new ElementIdEntity(elementDomain, elementId);
		ElementEntity existing = this.elementDao.findById(elementIdEntity)
				.orElseThrow(() -> new ObjectNotFoundException(
						"could not find object by elementDomain: " + elementDomain + "or elementId: " + elementId));

		if (update.getActive() != null)
			existing.setActive(update.getActive());

		if (update.getName() != null)
			existing.setName(update.getName());

		if (update.getLocation() != null)
			existing.setLocation(update.getLocation());

		if (update.getType() != null)
			existing.setType(update.getType());

		if (update.getElementAttributes() != null)
			existing.setElementAttributes(update.getElementAttributes());

		return this.converter.fromEntity(this.elementDao.save(existing));

	}

	@Override
	@Transactional(readOnly = true)
	@PerformanceMeasuring(units = PerformenceUnits.ns)
	public List<ElementBoundary> getAll(String userDomain, String userEmail) {

		ServiceTools.stringValidation(userDomain, userEmail);

		Iterable<ElementEntity> allElements = this.elementDao.findAll();

		List<ElementBoundary> returnElements = new ArrayList<>();

		for (ElementEntity entity : allElements)
			returnElements.add(this.converter.fromEntity(entity)); // map entities to boundaries

		return returnElements;

	}

	@Override
	@Transactional(readOnly = true)
	@PerformanceMeasuring(units = PerformenceUnits.ns)
	public List<ElementBoundary> getAll(String userDomain, String userEmail, int size, int page) {

		ServiceTools.stringValidation(userDomain, userEmail);

		ServiceTools.validatePaging(size, page);

		UserIdEntity uib = new UserIdEntity(userDomain, userEmail);
		UserEntity existingUser = this.userDao.findById(uib).orElseThrow(() -> new ObjectNotFoundException(
				"could not find object by UserDomain:" + userDomain + "or userEmail:" + userEmail));

		// if user is MANAGER : findAll
		if (existingUser.getRole().equals(UserRoleEntityEnum.manager))
			return this.elementDao.findAll(PageRequest.of(page, size, Direction.DESC, "elementId")) // Page<ElementEntity>
					.getContent() // List<ElementEntity>
					.stream() // Stream<ElementEntity>
					.map(this.converter::fromEntity) // Stream<ElementBoundary>
					.collect(Collectors.toList()); // List<ElementBoundary>

		// if user = PLAYER : findAllByActive
		if (existingUser.getRole().equals(UserRoleEntityEnum.player))
			return this.elementDao
					.findAllByActive(Boolean.TRUE, PageRequest.of(page, size, Direction.DESC, "elementId")) // Page<ElementEntity>
					.stream() // Stream<ElementEntity>
					.map(this.converter::fromEntity) // Stream<ElementBoundary>
					.collect(Collectors.toList()); // List<ElementBoundary>

		if (existingUser.getRole().equals(UserRoleEntityEnum.admin))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin User Can't get all Elements ");

		return new ArrayList<>();
	}

	@Override
	@Transactional(readOnly = true)
	@PerformanceMeasuring(units = PerformenceUnits.ns)
	public ElementBoundary getSpecificElement(String userDomain, String userEmail, String elementDomain,
			String elementId) {

		ServiceTools.stringValidation(userDomain, userEmail, elementDomain, elementId);

		UserEntity uE = this.userDao.findById(new UserIdEntity(userDomain, userEmail))
				.orElseThrow(() -> new ObjectNotFoundException(
						"could not find user by userDomain: " + userDomain + "and userEmail: " + userEmail));
		if (uE.getRole().equals(UserRoleEntityEnum.admin))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin User Can't Get Specific Element");

		ElementEntity existing = this.elementDao.findById(new ElementIdEntity(elementDomain, elementId))
				.orElseThrow(() -> new ObjectNotFoundException(
						"could not find object by elementDomain: " + elementDomain + "or elementId: " + elementId));

		if (uE.getRole().equals(UserRoleEntityEnum.player) && !existing.getActive())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player User Can't Get Specific Inactive Element");

		return this.converter.fromEntity(existing);

	}

	@Override
	@Transactional
	@PerformanceMeasuring(units = PerformenceUnits.ns)
	public void deleteAllElements(String adminDomain, String adminEmail) {

		ServiceTools.stringValidation(adminDomain, adminEmail);

		UserEntity uE = this.userDao.findById(new UserIdEntity(adminDomain, adminEmail))
				.orElseThrow(() -> new ObjectNotFoundException(
						"could not find user by userDomain: " + adminDomain + "and userEmail: " + adminEmail));

		if (!uE.getRole().equals(UserRoleEntityEnum.admin))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "only Admin can delete elements");

		this.elementDao.deleteAll();
	}

	@Override
	@Transactional
	@PerformanceMeasuring(units = PerformenceUnits.ns)
	public void bindExistingElementToAnExsitingChildElement(String managerDomain, String managerEmail,
			ElementIdBoundary originId, ElementIdBoundary responseId) {

		UserEntity uE = this.userDao.findById(new UserIdEntity(managerDomain, managerEmail))
				.orElseThrow(() -> new ObjectNotFoundException(
						"could not find user by userDomain: " + managerDomain + "and userEmail: " + managerEmail));

		if (uE.getRole().equals(UserRoleEntityEnum.admin))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "only manager can bind elements");

		ElementEntity origin = this.elementDao.findById(converter.fromElementIdBoundary(originId))
				.orElseThrow(() -> new ObjectNotFoundException("could not find origin by id:" + originId));

		ElementEntity response = this.elementDao.findById(converter.fromElementIdBoundary(responseId))
				.orElseThrow(() -> new ObjectNotFoundException("could not find origin by id:" + originId));

		if ((!origin.getActive() || !response.getActive()) && uE.getRole().equals(UserRoleEntityEnum.player))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
					"both elemntes must be active when A user player bind them!\n");

		response.setParent(origin);
		origin.addResponse(response);

		this.elementDao.save(origin);
		this.elementDao.save(response);

	}

	@Override
	@Transactional(readOnly = true)
	@PerformanceMeasuring
	public Set<ElementBoundary> getAllChildrenOfAnExsitingElement(String userDomain, String userEmail,
			String elementDomain, String elementId, int size, int page) {

		ServiceTools.validatePaging(size, page);

		ServiceTools.stringValidation(elementDomain, elementId, userDomain, userEmail);

		UserEntity uE = this.userDao.findById(new UserIdEntity(userDomain, userEmail))
				.orElseThrow(() -> new ObjectNotFoundException(
						"could not find user by userDomain: " + userDomain + "and userEmail: " + userEmail));

		if (uE.getRole().equals(UserRoleEntityEnum.admin))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin can't get all element children \n");

		if (uE.getRole().equals(UserRoleEntityEnum.manager))
			return this.elementDao
					.findAllByParent_ElementId_IdAndParent_ElementId_ElementDomain(elementId, elementDomain,
							PageRequest.of(page, size, Direction.DESC, "elementId"))
					.stream().map(this.converter::fromEntity).collect(Collectors.toSet());
		if (uE.getRole().equals(UserRoleEntityEnum.player))
		{
			//getting the parent
			ElementEntity parent = this.elementDao.findById(new ElementIdEntity(elementDomain,elementId))
					.orElseThrow(() -> new ObjectNotFoundException(
							"could not find user by elementDomain: " + elementDomain + "and elementId: " + elementId));
			//check if the parent is active
			if(!parent.getActive())
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "for player, the parent must be active \n");
			//getting all children
			Set<ElementBoundary> children = this.elementDao
					.findAllByParent_ElementId_IdAndParent_ElementId_ElementDomain(elementId, elementDomain,
							PageRequest.of(page, size, Direction.DESC, "elementId"))
					.stream().map(this.converter::fromEntity).collect(Collectors.toSet());
			//check if all children are active
			
			for (ElementBoundary elementBoundary : children) {
				if(!elementBoundary.getActive())
					throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "for player, all children must be active\n");
			}
			return children;
		}
		return new HashSet<ElementBoundary>();
	}

	@Override
	@Transactional(readOnly = true)
	@PerformanceMeasuring
	public Collection<ElementBoundary> getAnArrayWithElementParent(String userDomain, String userEmail,
			String elementDomain, String elementId, int size, int page) {

		ServiceTools.validatePaging(size, page);

		UserEntity uE = this.userDao.findById(new UserIdEntity(userDomain, userEmail))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"could not find user by userDomain: " + userDomain + " and userEmail: " + userEmail));
		
		if (uE.getRole().equals(UserRoleEntityEnum.admin))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin can't get all element children \n");

		//this is not needed, manager can 
		/*if (!uE.getRole().equals(UserRoleEntityEnum.manager))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "only manager can get all parents ");*/

		ElementEntity child = this.elementDao.findById(new ElementIdEntity(elementDomain, elementId))
				.orElseThrow(() -> new ObjectNotFoundException("could not find response by id:" + elementId));

		ElementEntity origin = child.getParent();

		if(uE.getRole().equals(UserRoleEntityEnum.player)) {
			if(!child.getActive()||!origin.getActive())
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "for player, both parent and child must be active\n");
		}
		
		Collection<ElementBoundary> rv = new HashSet<>();
		if (page > 1)
			return rv;
		if (origin != null && page == 0) {
			ElementBoundary rvBoundary = this.converter.fromEntity(origin);
			rv.add(rvBoundary);
		}
		return rv;

	}

	@Override
	@Transactional(readOnly = true)
	@PerformanceMeasuring
	public List<ElementBoundary> getElementsByName(String userDomain, String userEmail, String name, int size,
			int page) {
		ServiceTools.stringValidation(userDomain, userEmail, name);
		ServiceTools.validatePaging(size, page);
		UserEntity uE = this.userDao.findById(new UserIdEntity(userDomain, userEmail))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"could not find user by userDomain: " + userDomain + " and userEmail: " + userEmail));
		if (uE.getRole().equals(UserRoleEntityEnum.admin))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin User Can't Search Elements By Location");
		if (uE.getRole().equals(UserRoleEntityEnum.player))
			return this.elementDao
					.findAllByNameAndActive(name, true, PageRequest.of(page, size, Direction.DESC, "elementId"))
					.stream().map(this.converter::fromEntity).collect(Collectors.toList());
		if (uE.getRole().equals(UserRoleEntityEnum.manager))
			return this.elementDao.findAllByName(name, PageRequest.of(page, size, Direction.DESC, "elementId")).stream()
					.map(this.converter::fromEntity).collect(Collectors.toList());
		return new ArrayList<>();
	}

	@Override
	@Transactional(readOnly = true)
	@PerformanceMeasuring
	public Collection<ElementBoundary> searchByLocation(String userDomain, String userEmail, double lat, double lng,
			double distance, int size, int page) {

		ServiceTools.validatePaging(size, page);

		ServiceTools.stringValidation(userDomain, userEmail);

		UserEntity uE = this.userDao.findById(new UserIdEntity(userDomain, userEmail))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"could not find user by userDomain: " + userDomain + " and userEmail: " + userEmail));

		if (uE.getRole().equals(UserRoleEntityEnum.manager))
			return this.elementDao
					.findAllByLocation_LatBetweenAndLocation_LngBetween(lat - distance, lat + distance, lng - distance,
							lng + distance, PageRequest.of(page, size, Direction.ASC, "elementId"))
					.stream().map(this.converter::fromEntity).collect(Collectors.toList());

		if (uE.getRole().equals(UserRoleEntityEnum.player))
			return this.elementDao
					.findAllByLocation_LatBetweenAndLocation_LngBetweenAndActive(lat - distance, lat + distance,
							lng - distance, lng + distance, true,
							PageRequest.of(page, size, Direction.ASC, "elementId"))
					.stream().map(this.converter::fromEntity).collect(Collectors.toList());

		if (uE.getRole().equals(UserRoleEntityEnum.admin))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin User Can't Search Elements By Location");

		return new ArrayList<>();
	}



	@Override
	@Transactional(readOnly = true)
	@PerformanceMeasuring
	public Collection<ElementBoundary> searchByLocationAndType(String userDomain, String userEmail, double lat,
			double lng, double distance, String type, int size, int page) {

		ServiceTools.validatePaging(size, page);

		ServiceTools.stringValidation(userDomain, userEmail);

		UserEntity uE = this.userDao.findById(new UserIdEntity(userDomain, userEmail))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"could not find user by userDomain: " + userDomain + " and userEmail: " + userEmail));

		if (uE.getRole().equals(UserRoleEntityEnum.admin))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
					"Admin User Can't Search Elements By Location And Type");

		if (uE.getRole().equals(UserRoleEntityEnum.manager))
			return this.elementDao
					.findAllByLocation_LatBetweenAndLocation_LngBetweenAndType(lat - distance, lat + distance,
							lng - distance, lng + distance, type,
							PageRequest.of(page, size, Direction.ASC, "elementId"))
					.stream().map(this.converter::fromEntity).collect(Collectors.toList());

		if (uE.getRole().equals(UserRoleEntityEnum.player))
			return this.elementDao
					.findAllByLocation_LatBetweenAndLocation_LngBetweenAndActiveAndType(lat - distance, lat + distance,
							lng - distance, lng + distance, true, type,
							PageRequest.of(page, size, Direction.ASC, "elementId"))
					.stream().map(this.converter::fromEntity).collect(Collectors.toList());

		return new ArrayList<>();
	}

	@Override
	@Transactional(readOnly = true)
	@PerformanceMeasuring
	public List<ElementBoundary> getElementsByType(String userDomain, String userEmail, String type, int size,
			int page) {

		UserEntity uE = this.userDao.findById(new UserIdEntity(userDomain, userEmail))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"could not find user by userDomain: " + userDomain + " and userEmail: " + userEmail));

		if (uE.getRole().equals(UserRoleEntityEnum.manager))
			return this.elementDao.findAllByType(type, PageRequest.of(page, size, Direction.ASC, "elementId")).stream()
					.map(this.converter::fromEntity).collect(Collectors.toList());

		if (uE.getRole().equals(UserRoleEntityEnum.player))
			return this.elementDao
					.findAllByTypeAndActive(type, true, PageRequest.of(page, size, Direction.ASC, "elementId")).stream()
					.map(this.converter::fromEntity).collect(Collectors.toList());

		if (uE.getRole().equals(UserRoleEntityEnum.admin))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin User Can't Search Elements By Type");

		return new ArrayList<>();
	}

}