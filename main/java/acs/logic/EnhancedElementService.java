package acs.logic;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import acs.rest.boundaries.element.ElementBoundary;
import acs.rest.boundaries.element.ElementIdBoundary;

public interface EnhancedElementService extends ElementService {
	//

	public void bindExistingElementToAnExsitingChildElement(String managerDomain, String managerEmail,
			ElementIdBoundary originId, ElementIdBoundary responseId);

	public Set<ElementBoundary> getAllChildrenOfAnExsitingElement(String userDomain, String userEmail,
			String elementDomain, String elementId, int size, int page);

	public Collection<ElementBoundary> getAnArrayWithElementParent(String userDomain, String userEmail,
			String elementDomain, String elementId, int size, int page);

	public List<ElementBoundary> getElementsByName(String userDomain, String userEmail, String name, int size,
			int page);

	public List<ElementBoundary> getAll(String userDomain, String userEmail, int size, int page);

	public Collection<ElementBoundary> searchByLocation(String userDomain, String userEmail, double lat, double lng,
			double distance, int size, int page);

	public Collection<ElementBoundary> searchByLocationAndType(String userDomain, String userEmail, double lat,
			double lng, double distance, String type, int size, int page);

	List<ElementBoundary> getElementsByType(String userDomain, String userEmail, String type, int size, int page);

}
