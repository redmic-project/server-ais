package es.redmic.test.ais.unit.exceptions;

import java.io.IOException;

import org.junit.Test;

import es.redmic.ais.exceptions.ExceptionType;
import es.redmic.ais.exceptions.InvalidUsernameException;

public class InvalidUsernameExceptionTest extends BaseExceptionTest {

	@Test
	public void checkPattern_IsEqualToMessage_WhenNoLocaleSet() throws IOException {

		checkMessage(new InvalidUsernameException(), ExceptionType.INVALID_USERNAME_OR_PASSWORD.toString(), null);
	}
}