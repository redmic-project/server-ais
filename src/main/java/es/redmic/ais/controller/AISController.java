package es.redmic.ais.controller;

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

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import es.redmic.ais.service.AISService;

@Configuration
@ConditionalOnProperty(value = "app.scheduling.enable", havingValue = "true", matchIfMissing = true)
@EnableScheduling
public class AISController {

	private static Logger logger = LogManager.getLogger();

	@Value("${config.fixedDelay}")
	Long fixedDelay;

	@Autowired
	AISService aisService;

	@Scheduled(fixedDelayString = "${config.fixedDelay}", initialDelayString = "${config.fixedDelay}")
	public void execute() {

		logger.info(
				"Descargando datos, próxima descarga en " + TimeUnit.MILLISECONDS.toSeconds(fixedDelay) + " segundos.");

		aisService.fetchData();
	}
}
