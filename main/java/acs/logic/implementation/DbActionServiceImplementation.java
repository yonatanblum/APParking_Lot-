package acs.logic.implementation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import acs.dal.ActionDao;
import acs.dal.ElementDao;
import acs.dal.UserDao;
import acs.data.ActionEntity;
import acs.data.Converter;
import acs.data.ElementEntity;
import acs.data.ElementIdEntity;
import acs.data.UserRole;
import acs.logic.EnhancedActionService;
import acs.logic.EnhancedElementService;
import acs.logic.EnhancedUserService;
import acs.logic.ObjectNotFoundException;
import acs.logic.ServiceTools;
import acs.rest.boundaries.action.ActionAttribute;
import acs.rest.boundaries.action.ActionBoundary;
import acs.rest.boundaries.action.ActionIdBoundary;
import acs.rest.boundaries.action.ActionType;
import acs.rest.boundaries.element.ParkingLotAttributes;
import acs.rest.boundaries.element.ElementBoundary;
import acs.rest.boundaries.element.ElementIdBoundary;
import acs.rest.boundaries.element.ElementType;
import acs.rest.boundaries.element.Location;
import acs.rest.boundaries.element.ParkingAttributes;
import acs.rest.boundaries.user.UserBoundary;

@Service
public class DbActionServiceImplementation implements EnhancedActionService {
	private String projectName;
	private ActionDao actionDao;
	private ElementDao elementDao;
	private UserDao userDao;
	private Converter converter;
	private EnhancedElementService elementService;
	private EnhancedUserService userService;

	@Autowired
	public DbActionServiceImplementation(UserDao userDao, ActionDao actionDao, ElementDao elementDao,
			Converter converter, EnhancedUserService userService, EnhancedElementService elementService) {
		this.converter = converter;
		this.actionDao = actionDao;
		this.elementDao = elementDao;
		this.userDao = userDao;
		this.elementService = elementService;
		this.userService = userService;
	}

	// injection of project name from the spring boot configuration
	@Value("${spring.application.name: generic}")
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	@Override
	@Transactional // (readOnly = false)
	public Object invokeAction(ActionBoundary action) {

		if (action == null || action.getType() == null)
			throw new RuntimeException("ActionBoundary received in invokeAction method can't be null\n");

		UserBoundary userBoundary = this.userService.login(action.getInvokedBy().getUserId().getDomain(),
				action.getInvokedBy().getUserId().getEmail());

		if (!userBoundary.getRole().equals(UserRole.PLAYER))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "only player can invoke action");

		ElementBoundary element = this.elementService.getSpecificElement(action.getInvokedBy().getUserId().getDomain(),
				action.getInvokedBy().getUserId().getEmail(), action.getElement().getElementId().getDomain(),
				action.getElement().getElementId().getId());

		if (!element.getActive())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "element of action must be active");

		checkAction(action, element);

//		update location of car in db
		updateCarLocation(action, userBoundary, element);

		if (action.getType().toLowerCase().equals(ActionType.park.name())) {
			ElementBoundary parkingElement = parkOrDepart(element, userBoundary, false, action);
			saveAction(action);
			return parkingElement;
		}
		if (action.getType().toLowerCase().equals(ActionType.depart.name())) {
			ElementBoundary parkingElement = parkOrDepart(element, userBoundary, true, action);
			saveAction(action);
			return parkingElement;
		}

		if (action.getType().toLowerCase().equals(ActionType.search.name())) {
			ElementBoundary elementArr[] = search(element, userBoundary, 0.02, action);
			saveAction(action);
			return elementArr;
		}

		saveAction(action);
		return action;

	}

	public void updateCarLocation(ActionBoundary action, UserBoundary ue, ElementBoundary element) {
		HashMap<String, Double> location = action.getActionAttributes().containsKey(ActionAttribute.location.name())
				? (HashMap<String, Double>) action.getActionAttributes().get(ActionAttribute.location.name())
				: new HashMap<>();

		if (!location.isEmpty())
			element.setLocation(
					new Location(location.get(ActionAttribute.lat.name()), location.get(ActionAttribute.lng.name())));

		toManager(ue);
		elementService.update(ue.getUserId().getDomain(), ue.getUserId().getEmail(), element.getElementId().getDomain(),
				element.getElementId().getId(), element);
		toPlayer(ue);
	}

	public void saveAction(ActionBoundary action) {
		ActionIdBoundary aib = new ActionIdBoundary(projectName, UUID.randomUUID().toString());
		action.setCreatedTimestamp(new Date());
		action.setActionId(aib);
		ActionEntity entity = converter.toEntity(action);
		this.actionDao.save(entity);
	}

	public ElementBoundary[] search(ElementBoundary car, UserBoundary user, double distance, ActionBoundary action) {

		return Stream
				.concat(Arrays.stream(elementService.searchByLocationAndType(user.getUserId().getDomain(),
						user.getUserId().getEmail(), car.getLocation().getLat(), car.getLocation().getLng(), distance,
						ElementType.parking.name(), 36, 0).toArray(new ElementBoundary[0])),
						Arrays.stream(elementService.searchByLocationAndType(user.getUserId().getDomain(),
								user.getUserId().getEmail(), car.getLocation().getLat(), car.getLocation().getLng(),
								distance * 3, ElementType.parking_lot.name(), 36, 0).toArray(new ElementBoundary[0])))
				.toArray(ElementBoundary[]::new);
	}

	public void checkAction(ActionBoundary action, ElementBoundary element) {

		if (!element.getType().equals(ElementType.car.name())
				&& (action.getType().equals(ActionType.search.name()) || action.getType().equals(ActionType.park.name())
						|| action.getType().equals(ActionType.depart.name())))
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
					"park /search/ deprat can only invoke on car element");

	}

	public ElementBoundary parkOrDepart(ElementBoundary car, UserBoundary user, boolean depart, ActionBoundary action) {

		ElementBoundary parkingBoundary = null;
		double distanceFromCar = 0.0002;

		user = toManager(user);

		ElementBoundary[] allreadyPark = elementService
				.getAnArrayWithElementParent(user.getUserId().getDomain(), user.getUserId().getEmail(),
						car.getElementId().getDomain(), car.getElementId().getId(), 1, 0)
				.toArray(new ElementBoundary[0]);

		if (allreadyPark.length > 0)
			if (allreadyPark[0].getType().equals(ElementType.parking.toString()))
				parkingBoundary = updateParking(car, depart, user, allreadyPark);
			else if (allreadyPark[0].getType().equals(ElementType.parking_lot.toString()))
				parkingBoundary = updateParkingLot(car, depart, user, allreadyPark);

		if (parkingBoundary == null) {

			user = toPlayer(user);
//Searching for nearby parking to occupy 
			ElementBoundary[] parkingNearby = this.elementService.searchByLocationAndType(user.getUserId().getDomain(),
					user.getUserId().getEmail(), car.getLocation().getLat(), car.getLocation().getLng(),
					distanceFromCar, ElementType.parking.name(), 20, 0).toArray(new ElementBoundary[0]);

			ElementBoundary[] parkingLotNearBy = this.elementService
					.searchByLocationAndType(user.getUserId().getDomain(), user.getUserId().getEmail(),
							car.getLocation().getLat(), car.getLocation().getLng(), distanceFromCar * 4,
							ElementType.parking_lot.name(), 20, 0)
					.toArray(new ElementBoundary[0]);

			user = toManager(user);

			if (parkingLotNearBy.length > 0)
				parkingBoundary = updateParkingLot(car, depart, user, parkingLotNearBy);

			if (parkingNearby.length > 0 && parkingBoundary == null)
				parkingBoundary = updateParking(car, depart, user, parkingNearby);

//		if we didn't found parking nearby -> create new one
			if (parkingBoundary == null)
				parkingBoundary = createParking(car, depart, user);
		}

		toPlayer(user);

		return parkingBoundary;
	}

	public void unBindOrBindElements(ElementIdBoundary parking, ElementIdBoundary car, boolean unBind,
			UserBoundary userBoundary) {
//  	depart == true --> unbind
		if (unBind) {
			ElementEntity carEntity = elementDao.findById(new ElementIdEntity(car.getDomain(), car.getId()))
					.orElseThrow(() -> new ObjectNotFoundException("could not find object by elementDomain: "
							+ car.getDomain() + "or elementId: " + car.getId()));

			ElementEntity parkingEntity = elementDao.findById(new ElementIdEntity(parking.getDomain(), parking.getId()))
					.orElseThrow(() -> new ObjectNotFoundException("could not find object by elementDomain: "
							+ parking.getDomain() + "or elementId: " + parking.getId()));

			Set<ElementEntity> allCars = (Set<ElementEntity>) parkingEntity.getResponses();
			allCars.remove(carEntity);
			parkingEntity.setResponses(allCars);
			carEntity.setParent(null);
			this.elementDao.save(carEntity);

//		unBind == false --> bind
		} else
			this.elementService.bindExistingElementToAnExsitingChildElement(userBoundary.getUserId().getDomain(),
					userBoundary.getUserId().getEmail(), parking, car);

	}

	public boolean areEqual(ElementIdBoundary elementId_1, ElementIdBoundary elementId_2) {

		if (elementId_1 == null || elementId_2 == null)
			return false;

		if (elementId_1.getDomain() != elementId_2.getDomain() || elementId_1.getId() != elementId_2.getId())
			return false;

		return true;

	}

	public UserBoundary toPlayer(UserBoundary user) {
		user.setRole(UserRole.PLAYER);
//		return userService.updateUser(user.getUserId().getDomain(), user.getUserId().getEmail(), user);
		return converter.fromEntity(this.userDao.save(converter.toEntity(user)));

	}

	public UserBoundary toManager(UserBoundary user) {
		user.setRole(UserRole.MANAGER);
		return converter.fromEntity(this.userDao.save(converter.toEntity(user)));
//		return userService.updateUser(user.getUserId().getDomain(), user.getUserId().getEmail(), user);

	}

	public ElementBoundary updateParking(ElementBoundary car, boolean depart, UserBoundary userBoundary,
			ElementBoundary... parkingNearby) {
		ElementBoundary parkingBoundary = ServiceTools.getClosest(car, parkingNearby);

		ElementEntity parkingEntity = elementDao
				.findById(new ElementIdEntity(parkingBoundary.getElementId().getDomain(),
						parkingBoundary.getElementId().getId()))
				.orElseThrow(() -> new ObjectNotFoundException("could not find object by elementDomain: "
						+ car.getElementId().getDomain() + "or elementId: " + car.getElementId().getId()));

//		can't park where you already parking
		if (!parkingEntity.getActive() && !depart)
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
					"You cannot park when you are already parked ;<");

		if (depart && !parkingEntity.getActive())
			unBindOrBindElements(converter.toElementIdBoundary(parkingBoundary.getElementId().toString()),
					converter.toElementIdBoundary(car.getElementId().toString()), depart, userBoundary);

		if (!depart && parkingEntity.getActive())
			unBindOrBindElements(converter.toElementIdBoundary(parkingBoundary.getElementId().toString()),
					converter.toElementIdBoundary(car.getElementId().toString()), depart, userBoundary);

		if (depart && parkingEntity.getActive())
			if (!(parkingEntity.getLocation().getLat().equals(car.getLocation().getLat()))
					|| (!parkingEntity.getLocation().getLng().equals(car.getLocation().getLng())))
				return null;

		parkingEntity.setActive(depart);
		parkingEntity.getElementAttributes().put(ParkingAttributes.lastReportTimestamp.name(), new Date().toString());

		parkingEntity.getElementAttributes().put(ParkingAttributes.LastCarReport.name(),
				new ElementIdBoundary(car.getElementId().getDomain(), car.getElementId().getId()));

		return converter.fromEntity(this.elementDao.save(parkingEntity));
	}

	public ElementBoundary createParking(ElementBoundary car, boolean depart, UserBoundary userBoundary) {

		HashMap<String, Object> currentParkingAttributes = new HashMap<>();

		currentParkingAttributes.put(ParkingAttributes.LastCarReport.name(),
				new ElementIdBoundary(car.getElementId().getDomain(), car.getElementId().getId()));
		currentParkingAttributes.put(ParkingAttributes.lastReportTimestamp.name(), new Date().toString());

		ElementBoundary parkingBoundary = new ElementBoundary(new ElementIdBoundary("", ""), ElementType.parking.name(),
				"parking_name", depart, new Date(), car.getLocation(), currentParkingAttributes, car.getCreatedBy());

		parkingBoundary = this.elementService.create(userBoundary.getUserId().getDomain(),
				userBoundary.getUserId().getEmail(), parkingBoundary);

		unBindOrBindElements(parkingBoundary.getElementId(), car.getElementId(), depart, userBoundary);

		return parkingBoundary;
	}

	public ElementBoundary updateParkingLot(ElementBoundary car, Boolean depart, UserBoundary userBoundary,
			ElementBoundary... parkingLotNearBy) {

		ElementBoundary parkingBoundary = ServiceTools.getClosest(car, parkingLotNearBy);

//		this included in the version of the project pass to eyal
//		if (!parkingBoundary.getActive() )
//			return null;

		if (!parkingBoundary.getActive() && !depart)
			return null;

		List<String> carList = new ArrayList<>();
		int counter = 0, capacity = 0;

		parkingLotParkValidation(parkingBoundary);

		counter = (int) parkingBoundary.getElementAttributes().get(ParkingLotAttributes.carCounter.name());

		carList = (ArrayList<String>) parkingBoundary.getElementAttributes().get(ParkingLotAttributes.carList.name());

		capacity = (int) parkingBoundary.getElementAttributes().get(ParkingLotAttributes.capacity.name());

//		want to park but already parking
		if (carList.contains(car.getElementId().toString()) && !depart)
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
					"You cannot park when you are already parked ;<");

		if (!carList.contains(car.getElementId().toString()) && depart)
			return null;

//		want to park and not register in parkinglot - allowed
		if (!carList.contains(car.getElementId().toString()) && !depart) {
			if (capacity < counter + 1)
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "this parking lot is full");

			unBindOrBindElements(parkingBoundary.getElementId(), car.getElementId(), depart, userBoundary);

			carList.add(car.getElementId().toString());

			parkingBoundary.getElementAttributes().put(ParkingLotAttributes.carCounter.name(), ++counter);

		}
//		want to depart and currently parking at the parking lot
		if (carList.contains(car.getElementId().toString()) && depart) {
			carList.remove(car.getElementId().toString());
			parkingBoundary.getElementAttributes().put(ParkingLotAttributes.carCounter.name(), --counter);

			unBindOrBindElements(parkingBoundary.getElementId(), car.getElementId(), depart, userBoundary);

		}

//		parking lot is full - not active
		if (counter >= capacity)
			parkingBoundary.setActive(false);
		else
			parkingBoundary.setActive(true);

		parkingBoundary.getElementAttributes().put(ParkingLotAttributes.lastReportTimestamp.name(),
				(new Date()).toString());

		parkingBoundary.getElementAttributes().put(ParkingLotAttributes.carList.name(), carList.toArray(new String[0]));

		return this.elementService.update(userBoundary.getUserId().getDomain(), userBoundary.getUserId().getEmail(),
				parkingBoundary.getElementId().getDomain(), parkingBoundary.getElementId().getId(), parkingBoundary);

	}

	public void parkingLotParkValidation(ElementBoundary parkingBoundary) {
		if (!parkingBoundary.getElementAttributes().containsKey("capacity"))
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "parking lot must have 'capacity' attribute");

		if (!parkingBoundary.getElementAttributes().containsKey("carList"))
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "parking lot must have 'carList' attribute");

		if (!parkingBoundary.getElementAttributes().containsKey("carCounter"))
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "parking lot must have 'carCounter' attribute");

	}

	@Override
	@Transactional(readOnly = true)
	public List<ActionBoundary> getAllActions(String adminDomain, String adminEmail) {

		ServiceTools.stringValidation(adminDomain, adminEmail);

		Iterable<ActionEntity> allActions = this.actionDao.findAll();

		List<ActionBoundary> rv = new ArrayList<>();
		for (ActionEntity ent : allActions)
			rv.add(this.converter.fromEntity(ent));

		return rv;

	}

	@Override
	@Transactional // (readOnly = false)
	public void deleteAllActions(String adminDomain, String adminEmail) {

		ServiceTools.stringValidation(adminDomain, adminEmail);

		UserBoundary uE = this.userService.login(adminDomain, adminEmail);

		if (!uE.getRole().equals(UserRole.ADMIN))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "only admin can delete all actions");

		this.actionDao.deleteAll();

	}

	@Override
	public List<ActionBoundary> getAllActions(String adminDomain, String adminEmail, int size, int page) {

		ServiceTools.stringValidation(adminDomain, adminEmail);

		UserBoundary uE = this.userService.login(adminDomain, adminEmail);

		if (!uE.getRole().equals(UserRole.ADMIN))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "only admin can get all actions");

		ServiceTools.validatePaging(size, page);

		return this.actionDao.findAll(PageRequest.of(page, size, Direction.DESC, "actionId"))// Page<ActionEntity>
				.getContent()// List<ActionEntity>
				.stream()// Stream<ActionEntity>
				.map(this.converter::fromEntity)// Stream<ActionEntity>
				.collect(Collectors.toList()); // List<ActionEntity>

	}
}
