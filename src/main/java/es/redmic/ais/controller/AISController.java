package es.redmic.ais.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import es.redmic.ais.service.AISService;

@Configuration
@EnableScheduling
public class AISController {

	@Autowired
	AISService aisService;

	@Scheduled(fixedDelayString = "${config.fixedDelay}", initialDelayString = "${config.fixedDelay}")
	public void execute() {

		aisService.update();
	}
}
