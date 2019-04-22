package es.redmic.test.ais.unit.exceptions;

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
