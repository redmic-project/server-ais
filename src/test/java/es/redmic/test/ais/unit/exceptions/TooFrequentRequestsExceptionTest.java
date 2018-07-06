package es.redmic.test.ais.unit.exceptions;

import java.io.IOException;

import org.junit.Test;

import es.redmic.ais.exceptions.ExceptionType;
import es.redmic.ais.exceptions.TooFrequentRequestsException;

public class TooFrequentRequestsExceptionTest extends BaseExceptionTest {

	@Test
	public void checkPattern_IsEqualToMessage_WhenNoLocaleSet() throws IOException {

		checkMessage(new TooFrequentRequestsException(), ExceptionType.TOO_FREQUENT_REQUESTS.toString(), null);
	}
}