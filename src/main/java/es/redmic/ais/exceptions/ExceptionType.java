package es.redmic.ais.exceptions;

import es.redmic.exception.common.ExceptionTypeItfc;

public enum ExceptionType implements ExceptionTypeItfc {

	// @formatter:off
	
	// EventSource
	INVALID_USERNAME_OR_PASSWORD(Constants.INVALID_USERNAME_OR_PASSWORD),
	TOO_FREQUENT_REQUESTS(Constants.TOO_FREQUENT_REQUESTS);
	
	// @formatter:on

	final String type;

	ExceptionType(String type) {
		this.type = type;
	}

	public static ExceptionType fromString(String text) {
		if (text != null) {
			for (ExceptionType b : ExceptionType.values()) {
				if (text.equalsIgnoreCase(b.type)) {
					return b;
				}
			}
		}
		throw new IllegalArgumentException(text + " has no corresponding value");
	}

	@Override
	public String toString() {
		return type;
	}

	private static class Constants {

		// @formatter:off
		public static final String INVALID_USERNAME_OR_PASSWORD = "InvalidUsernameException",
				TOO_FREQUENT_REQUESTS = "TooFrequentRequestsException";
		// @formatter:on
	}

}
