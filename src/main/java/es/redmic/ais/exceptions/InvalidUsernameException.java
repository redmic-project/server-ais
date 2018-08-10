package es.redmic.ais.exceptions;

import es.redmic.exception.common.BadRequestException;

public class InvalidUsernameException extends BadRequestException {

	private static final long serialVersionUID = 1L;

	public InvalidUsernameException() {
		super(ExceptionType.INVALID_USERNAME_OR_PASSWORD);
	}
}
