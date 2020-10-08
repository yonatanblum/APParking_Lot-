package acs.rest.boundaries.action;

import acs.rest.boundaries.element.ElementIdBoundary;

public class ElementOfAction {
	ElementIdBoundary elementId;

	public ElementOfAction(ElementIdBoundary element) {
		super();
		this.elementId = element;
	}

	public ElementOfAction() {

	}

	public ElementIdBoundary getElementId() {
		return elementId;
	}

	public void setElementId(ElementIdBoundary elementId) {
		this.elementId = elementId;
	}


}
