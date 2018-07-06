package es.redmic.ais.exceptions;

import es.redmic.exception.common.BadRequestException;

public class TooFrequentRequestsException extends BadRequestException {

	private static final long serialVersionUID = 1L;

	public TooFrequentRequestsException() {
		super(ExceptionType.TOO_FREQUENT_REQUESTS);
	}

}
