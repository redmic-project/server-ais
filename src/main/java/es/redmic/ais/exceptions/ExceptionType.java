package es.redmic.ais.exceptions;

/*-
 * #%L
 * AIS
 * %%
 * Copyright (C) 2019 REDMIC Project / Server
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
