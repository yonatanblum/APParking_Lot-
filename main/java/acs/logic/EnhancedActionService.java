package acs.logic;

import java.util.List;

import acs.rest.boundaries.action.ActionBoundary;

public interface EnhancedActionService extends ActionService {
	public List<ActionBoundary> getAllActions(String adminDomain, String adminEmail, int size, int page);
}
