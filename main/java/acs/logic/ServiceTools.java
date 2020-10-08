package acs.logic;

import acs.rest.boundaries.element.ElementBoundary;

public class ServiceTools {
	private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

	public static double distance(double startLat, double startLong, double endLat, double endLong) {

		double dLat = Math.toRadians((endLat - startLat));
		double dLong = Math.toRadians((endLong - startLong));

		startLat = Math.toRadians(startLat);
		endLat = Math.toRadians(endLat);

		double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return EARTH_RADIUS * c; // <-- d
	}

	public static double haversin(double val) {
		return Math.pow(Math.sin(val / 2), 2);
	}

	static public void stringValidation(String... strings) {
		for (String string : strings)
			if (string == null || string.trim().isEmpty())
				throw new RuntimeException("Any Url String Variable Must Not Be Empty Or null");

	}

	static public void validatePaging(int size, int page) {
		if (size < 1)
			throw new RuntimeException("size must be not less than 1");

		if (page < 0)
			throw new RuntimeException("page must not be negative");

	}

	static public ElementBoundary getClosest(ElementBoundary car, ElementBoundary... parkingNearBy) {
		double minDistance = 100, tempDistance;
		ElementBoundary parkingBoundary = null;
		for (ElementBoundary elementBoundary : parkingNearBy) {
			tempDistance = distance(car.getLocation().getLat(), car.getLocation().getLng(),
					elementBoundary.getLocation().getLat(), elementBoundary.getLocation().getLng());
			if (tempDistance < minDistance) {
				minDistance = tempDistance;
				parkingBoundary = elementBoundary;
			}

		}
		return parkingBoundary;
	}

}
