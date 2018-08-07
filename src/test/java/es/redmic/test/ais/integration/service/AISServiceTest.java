package es.redmic.test.ais.integration.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import es.redmic.ais.AISApplication;
import es.redmic.ais.exceptions.InvalidUsernameException;
import es.redmic.ais.service.AISService;
import es.redmic.brokerlib.avro.geodata.tracking.vessels.AISTrackingDTO;
import es.redmic.exception.custom.ResourceNotFoundException;
import es.redmic.testutils.kafka.KafkaBaseIntegrationTest;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { AISApplication.class })
@ActiveProfiles("test")
@TestPropertySource(properties = { "schema.registry.port=18081" })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class AISServiceTest extends KafkaBaseIntegrationTest {

	@Autowired
	AISService aisService;

	@Value("${property.path.media_storage.AIS_TEMP}")
	private String directoryPath;

	@ClassRule
	public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1);

	protected BlockingQueue<Object> blockingQueue;

	@PostConstruct
	public void CreateVesselFromTrackingTestPostConstruct() throws Exception {
		createSchemaRegistryRestApp(embeddedKafka.getZookeeperConnectionString(), embeddedKafka.getBrokersAsString());
	}

	@Before
	public void setup() {

		blockingQueue = new LinkedBlockingDeque<>();
	}

	@Test(expected = ResourceNotFoundException.class)
	public void fetchData_ThrowException_IfServerIsNotAccessible() throws Exception {

		aisService.fetchData();
	}

	@Test
	public void processFile_ShouldSendMessageToKafka_IfDataIsCorrect() throws Exception {

		File srcFile = new File("src/test/resources/ais.zip");

		File destFile = new File(directoryPath + "/ais.zip");

		FileUtils.copyFile(srcFile, destFile);

		Whitebox.invokeMethod(aisService, "unzip");

		Whitebox.invokeMethod(aisService, "processFile");

		Thread.sleep(500);

		// Espera que se publiquen 23016 registros a kafka
		assertEquals(23016, blockingQueue.size());
	}

	@Test(expected = InvalidUsernameException.class)
	public void processFile_ThrowException_IfUsernameIsInvalid() throws Exception {

		File srcFile = new File("src/test/resources/invalidUsername_ais.zip");

		File destFile = new File(directoryPath + "/ais.zip");

		FileUtils.copyFile(srcFile, destFile);

		Whitebox.invokeMethod(aisService, "unzip");

		Whitebox.invokeMethod(aisService, "processFile");
	}

	@Test(expected = InvalidUsernameException.class)
	public void processFile_ThrowException_IfTooFrequentRequest() throws Exception {

		File srcFile = new File("src/test/resources/tooFrequentRequest_ais.zip");

		File destFile = new File(directoryPath + "/ais.zip");

		FileUtils.copyFile(srcFile, destFile);

		Whitebox.invokeMethod(aisService, "unzip");

		Whitebox.invokeMethod(aisService, "processFile");
	}

	@KafkaListener(topics = "${broker.topic.realtime.tracking.vessels}")
	public void updateVessels(AISTrackingDTO dto) {

		assertNotNull(dto);
		assertNotNull(dto.getMmsi());
		assertNotNull(dto.getLatitude());
		assertNotNull(dto.getLongitude());
		assertNotNull(dto.getTstamp());

		blockingQueue.offer(dto);
	}
}