package acs.rest;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import acs.logic.EnhancedElementService;
import acs.rest.boundaries.element.ElementBoundary;
import acs.rest.boundaries.element.ElementIdBoundary;

@RestController
public class ElementController {
	private EnhancedElementService elementService;

	@Autowired
	public ElementController() {

	}

	public ElementController(EnhancedElementService elementService) {
		super();
		this.elementService = elementService;
	}

	@Autowired
	public void setElementService(EnhancedElementService elementService) {
		this.elementService = elementService;
	}

	@RequestMapping(path = "/acs/elements/{managerDomain}/{managerEmail}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ElementBoundary CreateNewElement(@PathVariable("managerDomain") String managerDomain,
			@PathVariable("managerEmail") String managerEmail, @RequestBody ElementBoundary elementDetails) {
		return this.elementService.create(managerDomain, managerEmail, elementDetails);

	}

	@RequestMapping(path = "/acs/elements/{userDomain}/{userEmail}/{elementDomain}/{elementId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ElementBoundary RetreiveElement(@PathVariable("userDomain") String userDomain,
			@PathVariable("userEmail") String userEmail, @PathVariable("elementDomain") String elementDomain,
			@PathVariable("elementId") String elementId) {
		return this.elementService.getSpecificElement(userDomain, userEmail, elementDomain, String.valueOf(elementId));
	}

	@RequestMapping(path = "/acs/elements/{userDomain}/{userEmail}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ElementBoundary[] RetreiveElementArr(@PathVariable("userDomain") String userDomain,
			@PathVariable("userEmail") String userEmail,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		return this.elementService.getAll(userDomain, userEmail, size, page).toArray(new ElementBoundary[0]);
	}

	@RequestMapping(path = "/acs/elements/{managerDomain}/{managerEmail}/{elementDomain}/{elementId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void updateElementDetails(@PathVariable("managerDomain") String managerDomain,
			@PathVariable("managerEmail") String managerEmail, @PathVariable("elementDomain") String elementDomain,
			@PathVariable("elementId") String elementId, @RequestBody ElementBoundary update) {
		elementService.update(managerDomain, managerEmail, elementDomain, elementId, update);
	}

	@RequestMapping(path = "/acs/elements/{userDomain}/{userEmail}/{elementDomain}/{elementId}/children", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ElementBoundary[] getAllChildren(@PathVariable("userDomain") String userDomain,
			@PathVariable("userEmail") String userEmail, @PathVariable("elementDomain") String elementDomain,
			@PathVariable("elementId") String elementId,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		return this.elementService.getAllChildrenOfAnExsitingElement(userDomain, userEmail, elementDomain,
				String.valueOf(elementId), size, page).toArray(new ElementBoundary[0]);
	}

	@RequestMapping(path = "/acs/elements/{userDomain}/{userEmail}/{elementDomain}/{elementId}/parents", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ElementBoundary[] getAllParents(@PathVariable("userDomain") String userDomain,
			@PathVariable("userEmail") String userEmail, @PathVariable("elementDomain") String elementDomain,
			@PathVariable("elementId") String elementId,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		return this.elementService.getAnArrayWithElementParent(userDomain, userEmail, elementDomain,
				String.valueOf(elementId), size, page).toArray(new ElementBoundary[0]);
	}

	@RequestMapping(path = "/acs/elements/{managerDomain}/{managerEmail}/{elementDomain}/{elementId}/children", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void bindElements(@PathVariable("managerDomain") String managerDomain,
			@PathVariable("managerEmail") String managerEmail, @PathVariable("elementDomain") String elementDomain,
			@PathVariable("elementId") String elementId, @RequestBody ElementIdBoundary responseId) {
		this.elementService.bindExistingElementToAnExsitingChildElement(managerDomain, managerEmail,
				new ElementIdBoundary(elementDomain, elementId), responseId);
	}

	@RequestMapping(path = "/acs/elements/{UserDomain}/{UserEmail}/search/byName/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ElementBoundary[] searchElemntsByName(@PathVariable("UserDomain") String userDomain,
			@PathVariable("UserEmail") String userEmail, @PathVariable("name") String name,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		return this.elementService.getElementsByName(userDomain, userEmail, name, size, page).stream()
				.collect(Collectors.toList()).toArray(new ElementBoundary[0]);
	}

	@RequestMapping(path = "/acs/elements/{userDomain}/{userEmail}/search/near/{lat}/{lng}/{distance}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ElementBoundary[] searchByLocation(@PathVariable("userDomain") String userDomain,
			@PathVariable("userEmail") String userEmail, @PathVariable("lat") double lat,
			@PathVariable("lng") double lng, @PathVariable("distance") double distance,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {

		return this.elementService.searchByLocation(userDomain, userEmail, lat, lng, distance, size, page)
				.toArray(new ElementBoundary[0]);
	}

	@RequestMapping(path = "/acs/elements/{userDomain}/{userEmail}/search/byType/{type}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ElementBoundary[] getElementsByType(@PathVariable("userDomain") String userDomain,
			@PathVariable("userEmail") String userEmail, @PathVariable("type") String type,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		return this.elementService.getElementsByType(userDomain, userEmail, type, size, page)
				.toArray(new ElementBoundary[0]);
	}

}
