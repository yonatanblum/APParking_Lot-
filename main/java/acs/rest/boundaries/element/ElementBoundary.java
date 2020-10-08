package acs.rest.boundaries.element;

import java.util.Date;
import java.util.Map;
import acs.rest.boundaries.user.UserIdBoundary;

/*
{
    "elementId": {
        "domain": "2020b.tamir.reznik",
        "id": "5303776d-87d8-4d84-b8c3-b1240787e2a8"
    },
    "type": "demoElement",
    "name": "Parking Lot",
    "active": true,
    "timeStamp": "1970-01-01",
    "createdBy":
    	{"userId":{
			"domain":"2020b.tamir.reznik",
			"email":"aNNA@us.er"
			}
	}	,
    "location": {
        "lat": 35.3256,
        "lng": 46.0234
    },
    "elementAttributes": {
        "test": "great test",
        "parking type": "CRITICAL"
    }
}
 */

//Types : Parking , parking_lot , 
public class ElementBoundary {
	private ElementIdBoundary elementId;
	private String type;
	private String name;
	private Boolean active;
	private Date createdTimestamp;
	private Map<String, UserIdBoundary> createdBy;
	private Location location;
	private Map<String, Object> elementAttributes;

	public ElementBoundary(ElementIdBoundary elementId, String type, String name, Boolean active, Date timeStamp,
			Location location, Map<String, Object> elemntAttributes, Map<String, UserIdBoundary> createdBy) {
		super();
		this.elementId = elementId;
		this.type = type;
		this.name = name;
		this.active = active;
		this.createdTimestamp = timeStamp;
		this.createdBy = createdBy;
		this.location = location;
		this.elementAttributes = elemntAttributes;
	}

	public ElementBoundary() {
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Date getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(Date createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	public Map<String, Object> getElementAttributes() {
		return elementAttributes;
	}

	public void setElementAttributes(Map<String, Object> elementAttributes) {
		this.elementAttributes = elementAttributes;
	}

	public ElementIdBoundary getElementId() {
		return elementId;
	}

	public void setElementId(ElementIdBoundary elementId) {
		this.elementId = elementId;
	}

	public Location getLocation() {
		return location;
	}

	public Map<String, UserIdBoundary> getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Map<String, UserIdBoundary> createBy) {
		this.createdBy = createBy;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
}
