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
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import es.redmic.ais.AISApplication;
import es.redmic.ais.exceptions.InvalidUsernameException;
import es.redmic.ais.service.AISService;
import es.redmic.exception.custom.ResourceNotFoundException;
import es.redmic.testutils.kafka.KafkaBaseIntegrationTest;
import es.redmic.vesselslib.dto.ais.AISTrackingDTO;
import es.redmic.vesselslib.dto.tracking.VesselTrackingDTO;
import es.redmic.vesselslib.dto.vessel.VesselDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { AISApplication.class })
@ActiveProfiles("test")
@TestPropertySource(properties = { "schema.registry.port=18081" })
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class AISServiceTest extends KafkaBaseIntegrationTest {

	@Autowired
	AISService aisService;

	@Value("${property.path.media_storage.AIS_TEMP}")
	private String directoryPath;

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1);

	// @formatter:off

	protected BlockingQueue<Object> blockingQueueVesselTracking,
		blockingQueueAIS,
		blockingQueueVessel;

	// @formatter:on

	@PostConstruct
	public void AISServiceTestPostConstruct() throws Exception {
		createSchemaRegistryRestApp(embeddedKafka.getEmbeddedKafka().getZookeeperConnectionString(),
				embeddedKafka.getEmbeddedKafka().getBrokersAsString());
	}

	@Before
	public void setup() {

		blockingQueueVesselTracking = new LinkedBlockingDeque<>();
		blockingQueueAIS = new LinkedBlockingDeque<>();
		blockingQueueVessel = new LinkedBlockingDeque<>();
	}

	@Test(expected = ResourceNotFoundException.class)
	public void fetchData_ThrowException_IfServerIsNotAccessible() throws Exception {

		aisService.fetchData();
	}

	@Test
	public void processFile_ShouldSendMessageToKafka_IfDataIsCorrect() throws Exception {

		// Coloca el primer fichero de test en el lugar correspondiente
		File srcFile = new File("src/test/resources/ais.zip");
		File destFile = new File(directoryPath + "/ais.zip");
		FileUtils.copyFile(srcFile, destFile);

		// Invoca los métodos como si se hubiera ejecutado desde el schedule
		Whitebox.invokeMethod(aisService, "unzip");
		Whitebox.invokeMethod(aisService, "processFile");

		// Coloca el segundo fichero de test en el lugar correspondiente
		File srcFile2 = new File("src/test/resources/ais2.zip");
		File destFile2 = new File(directoryPath + "/ais2.zip");
		FileUtils.copyFile(srcFile2, destFile2);

		// Cambia nombre dentro del servicio e invoca los métodos de nuevo para procesar
		// el segundo fichero
		Whitebox.setInternalState(aisService, "nameCompressFile", "ais2.zip");
		Whitebox.invokeMethod(aisService, "unzip");
		Whitebox.invokeMethod(aisService, "processFile");

		// Espera un tiempo para que pueda terminar de procesar los dos ficheros
		Thread.sleep(23000);

		// @formatter:off

		int numOfItems = 41998, // Debería procesar 41976 pero repite 36 elementos que llegan en el segundo
				numOfItemsInBbox = 86; 
		// @formatter:on

		// fichero con el mismo tstamp

		// Espera que se publiquen numOfItems registros al topic de ais
		assertEquals(numOfItems, blockingQueueAIS.size());

		// Espera que se publiquen numOfItemsInBbox registros al topic de vesselTracking
		assertEquals(numOfItemsInBbox, blockingQueueVesselTracking.size());

		// Espera que se publiquen numOfItemsInBbox registros al topic de vessel
		assertEquals(numOfItemsInBbox, blockingQueueVessel.size());
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
	public void vesselTracking(VesselTrackingDTO dto) {

		assertNotNull(dto);
		assertNotNull(dto.getGeometry());
		assertNotNull(dto.getProperties().getDate());
		assertNotNull(dto.getProperties().getVessel().getMmsi());
		assertNotNull(dto.getProperties().getVessel().getType().getCode());

		blockingQueueVesselTracking.offer(dto);
	}

	@KafkaListener(topics = "${broker.topic.realtime.ais}")
	public void ais(AISTrackingDTO dto) {

		assertNotNull(dto);
		assertNotNull(dto.getMmsi());
		assertNotNull(dto.getLatitude());
		assertNotNull(dto.getLongitude());
		assertNotNull(dto.getTstamp());
		assertNotNull(dto.getType());

		blockingQueueAIS.offer(dto);
	}

	@KafkaListener(topics = "${broker.topic.realtime.vessels}")
	public void vessels(VesselDTO dto) {

		assertNotNull(dto);
		assertNotNull(dto.getMmsi());
		assertNotNull(dto.getType());

		blockingQueueVessel.offer(dto);
	}
}