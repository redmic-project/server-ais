package es.redmic.ais.controller;

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
