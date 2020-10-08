package acs.data;

import java.util.Date;
import java.util.Map;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "ACTIONS")
public class ActionEntity {
	private String actionId;
	private String type;
	private ElementIdEntity element;
	private Date createdTimestamp;
	private UserIdEntity invokedBy;
	private Map<String, Object> actionAttributes;

	public ActionEntity() {
	}

	public ActionEntity(String actionId, String type, ElementIdEntity element, Date timestamp, UserIdEntity invokedBy,
			Map<String, Object> actionAttributes) {
		super();
		this.actionId = actionId;
		this.type = type;
		this.element = element;
		this.createdTimestamp = timestamp;
		this.invokedBy = invokedBy;
		this.actionAttributes = actionAttributes;
	}

	@Id
	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getTimestamp() {
		return createdTimestamp;
	}

	@Embedded
	public ElementIdEntity getElement() {
		return element;
	}

	public void setElement(ElementIdEntity element) {
		this.element = element;
	}

	public void setTimestamp(Date timestamp) {
		this.createdTimestamp = timestamp;
	}

	// @Convert(converter = acs.dal.MapToJsonConverter.class)
	// @Lob
	@Embedded
	public UserIdEntity getInvokedBy() {
		return invokedBy;
	}

	public void setInvokedBy(UserIdEntity invokedBy) {
		this.invokedBy = invokedBy;
	}

	@Convert(converter = acs.dal.MapToJsonConverter.class)
	@Lob
	public Map<String, Object> getActionAttributes() {
		return actionAttributes;
	}

	public void setActionAttributes(Map<String, Object> actionAttributes) {
		this.actionAttributes = actionAttributes;
	}
}