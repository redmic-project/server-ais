package es.redmic.test.ais.integration.service;

import static org.junit.Assert.assertNotNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import es.redmic.ais.AISApplication;
import es.redmic.ais.service.AISService;
import es.redmic.brokerlib.avro.geodata.tracking.vessels.AISTrackingDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { AISApplication.class })
@ActiveProfiles("test")
@DirtiesContext
public class AISServiceTest {

	private static final String TOPIC = "realtime.tracking.vessels";

	@Autowired
	AISService aisService;

	@ClassRule
	public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, TOPIC);

	protected static BlockingQueue<Object> blockingQueue;

	@BeforeClass
	public static void setup() {

		blockingQueue = new LinkedBlockingDeque<>();
	}

	@Test
	public void shouldSendMessageToKafka() throws Exception {

		aisService.setDirectoryPath(System.getProperty("user.dir") + "/src/test/resources/");
		aisService.setNameFile("dataPrueba.csv");
		aisService.processFile();

		assertNotNull(blockingQueue.poll(5, TimeUnit.SECONDS));
	}

	@KafkaListener(topics = TOPIC)
	public void updateVessels(AISTrackingDTO dto) {

		assertNotNull(dto);

		blockingQueue.offer(dto);
	}
}
