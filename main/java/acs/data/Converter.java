package acs.data;

import org.springframework.stereotype.Component;

import acs.logic.ServiceTools;
import acs.rest.boundaries.action.ActionBoundary;
import acs.rest.boundaries.action.ActionIdBoundary;
import acs.rest.boundaries.action.ElementOfAction;
import acs.rest.boundaries.action.InvokingUser;
import acs.rest.boundaries.element.ElementBoundary;
import acs.rest.boundaries.element.ElementIdBoundary;
import acs.rest.boundaries.user.UserBoundary;
import acs.rest.boundaries.user.UserIdBoundary;

@Component
public class Converter {

	public ElementBoundary fromEntity(ElementEntity entity) {
		ElementIdBoundary elementIdBoundary = new ElementIdBoundary(entity.getElementId().getElementDomain(),
				entity.getElementId().getId());
		ElementBoundary eb = new ElementBoundary(elementIdBoundary, entity.getType(), entity.getName(),
				entity.getActive(), entity.getTimeStamp(), entity.getLocation(), entity.getElementAttributes(),
				entity.getCreateBy());
		return eb;
	}

	public ElementEntity toEntity(ElementBoundary boundary) {
		ElementEntity eE = new ElementEntity(
				new ElementIdEntity(boundary.getElementId().getDomain(), boundary.getElementId().getId()),
				boundary.getType(), boundary.getName(), boundary.getActive(), boundary.getCreatedTimestamp(),
				boundary.getLocation(), boundary.getElementAttributes(), boundary.getCreatedBy());
		return eE;
	}
	// domain :abc id: 123 -- > abc#123

	public UserBoundary fromEntity(UserEntity entity) {

		UserIdBoundary userId = fromEntity(entity.getUserId());

		UserBoundary ub = new UserBoundary(userId, null, entity.getUsername(), entity.getAvatar());

		if (entity.getRole() != null)
			ub.setRole(this.fromEntity(entity.getRole()));

		return ub;
	}

	public UserEntity toEntity(UserBoundary boundary) {

		UserRoleEntityEnum role = boundary.getRole() == null ? null : toEntity(boundary.getRole());

		UserIdEntity userId = toEntity(boundary.getUserId());

		UserEntity ue = new UserEntity(userId, role, boundary.getUsername(), boundary.getAvatar());

		return ue;
	}

	public UserRoleEntityEnum toEntity(UserRole type) {
		if (type != null) {
			return UserRoleEntityEnum.valueOf(type.name().toLowerCase());
		} else {
			return null;
		}
	}

	public UserRole fromEntity(UserRoleEntityEnum type) {
		if (type != null) {
			return UserRole.valueOf(type.name().toUpperCase());
		} else {
			return null;
		}
	}

	public UserIdBoundary fromEntity(UserIdEntity userId) {
		if (userId != null)
			return new UserIdBoundary(userId.getDomain(), userId.getEmail());
		else
			return null;
	}

	public UserIdEntity toEntity(UserIdBoundary userId) {

		ServiceTools.stringValidation(userId.getDomain(), userId.getEmail());

		return new UserIdEntity(userId.getDomain(), userId.getEmail());

	}

	public String typeEnumToString(TypeEnum type) {
		if (type != null) {
			return type.name();
		} else {
			return null;
		}
	}

	// keep this function for later use in case toString won't be enough
	public <T> String fromIdBoundary(T idBoundary) {

		if (idBoundary.getClass().equals(ActionIdBoundary.class))
			return ((ActionIdBoundary) idBoundary).getDomain() + "#" + ((ActionIdBoundary) idBoundary).getId();

		if (idBoundary.getClass().equals(ElementIdBoundary.class))
			return ((ElementIdBoundary) idBoundary).getDomain() + "#" + ((ElementIdBoundary) idBoundary).getId();

		if (idBoundary.getClass().equals(UserIdBoundary.class))
			return ((UserIdBoundary) idBoundary).getDomain() + "#" + ((UserIdBoundary) idBoundary).getEmail();

		return null;
	}

	public ActionEntity toEntity(ActionBoundary actionBoundary) {

		String type = actionBoundary.getType() == null ? null : actionBoundary.getType();

		ElementIdEntity idEntity = new ElementIdEntity(actionBoundary.getElement().getElementId().getDomain(),
				actionBoundary.getElement().getElementId().getId());

		return new ActionEntity(fromIdBoundary(actionBoundary.getActionId()), type, idEntity,
				actionBoundary.getCreatedTimestamp(), toEntity(actionBoundary.getInvokedBy().getUserId()),
				actionBoundary.getActionAttributes());
	}

	public ActionIdBoundary toActionIdBoundary(String entity) {
		if (entity != null && !entity.trim().isEmpty())
			return new ActionIdBoundary(entity.substring(0, entity.indexOf('#')),
					entity.substring(entity.indexOf('#') + 1));
		return null;
	}

	public ElementIdBoundary toElementIdBoundary(String entity) {
		if (entity != null && !entity.trim().isEmpty())
			return new ElementIdBoundary(entity.substring(0, entity.indexOf('#')),
					entity.substring(entity.indexOf('#') + 1));

		return null;
	}

	public ActionBoundary fromEntity(ActionEntity entity) {

		ActionIdBoundary actionIdBoundary = toActionIdBoundary(entity.getActionId());

		ElementOfAction element = new ElementOfAction(
				new ElementIdBoundary(entity.getElement().getElementDomain(), entity.getElement().getId()));

		ActionBoundary boundary = new ActionBoundary(actionIdBoundary, null, element, entity.getTimestamp(),
				new InvokingUser(fromEntity(entity.getInvokedBy())), entity.getActionAttributes());

		if (entity.getType() != null)
			boundary.setType(entity.getType());

		return boundary;

	}

	public ElementIdEntity fromElementIdBoundary(ElementIdBoundary boundary) {
		if (boundary.getDomain() != null && boundary.getId() != null)
			return new ElementIdEntity(boundary.getDomain(), boundary.getId());
		return null;
	}

}
