package acs.data;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import acs.dal.MapToJsonConverter;
import acs.rest.boundaries.element.Location;
import acs.rest.boundaries.user.UserIdBoundary;

@Entity
@Table(name = "ELEMENTS")
public class ElementEntity {

	private ElementIdEntity elementId;
	private String type;
	private String name;
	private Boolean active;
	private Date createdTimestamp;
	private Map<String, UserIdBoundary> createdBy;
	private Location location;
	private Map<String, Object> elementAttributes;
	private Set<ElementEntity> childrens;
	private ElementEntity parent;

	public ElementEntity() {

	}

	public ElementEntity(ElementIdEntity elementId, String type, String name, Boolean active, Date timeStamp,
			Location location, Map<String, Object> elementAttributes, Map<String, UserIdBoundary> createBy) {
		super();
		this.elementId = elementId;
		this.type = type;
		this.name = name;
		this.active = active;
		this.createdTimestamp = timeStamp;
		this.createdBy = createBy;
		this.location = location;
		this.elementAttributes = elementAttributes;
		this.childrens = new HashSet<ElementEntity>();
		this.parent = null;
	}

	@Embedded
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	@EmbeddedId
	public ElementIdEntity getElementId() {
		return elementId;
	}

	public void setElementId(ElementIdEntity elementId) {
		this.elementId = elementId;
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

	@Temporal(TemporalType.TIMESTAMP)
	public Date getTimeStamp() {
		return createdTimestamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.createdTimestamp = timeStamp;
	}

	@Convert(converter = MapToJsonConverter.class)
	@Lob
	public Map<String, UserIdBoundary> getCreateBy() {
		return createdBy;
	}

	public void setCreateBy(Map<String, UserIdBoundary> createBy) {
		this.createdBy = createBy;
	}

	@Convert(converter = MapToJsonConverter.class)
	@Lob
	public Map<String, Object> getElementAttributes() {
		return elementAttributes;
	}

	public void setElementAttributes(Map<String, Object> elementAttributes) {
		this.elementAttributes = elementAttributes;
	}

	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
	public Set<ElementEntity> getResponses() {
		return childrens;
	}

	public void setResponses(Set<ElementEntity> responses) {
		this.childrens = responses;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	public ElementEntity getParent() {
		return parent;
	}

	public void setParent(ElementEntity parent) {
		this.parent = parent;
	}

	public void addResponse(ElementEntity response) {
		this.childrens.add(response);
//		response.setParent(this);
	}
}
