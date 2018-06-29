package es.redmic.ais;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "es.redmic.ais", "es.redmic.brokerlib" })
public class AISApplication {

	public static void main(String[] args) {
		SpringApplication.run(AISApplication.class, args);
	}
}
