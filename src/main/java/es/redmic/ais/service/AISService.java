package es.redmic.ais.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import es.redmic.ais.exceptions.InvalidUsernameException;
import es.redmic.brokerlib.avro.common.CommonDTO;
import es.redmic.brokerlib.listener.SendListener;
import es.redmic.exception.custom.ResourceNotFoundException;
import es.redmic.utils.compressor.Zip;
import es.redmic.utils.csv.DataLoaderIngestData;
import es.redmic.vesselslib.dto.ais.AISTrackingDTO;
import es.redmic.vesselslib.dto.tracking.VesselTrackingDTO;
import es.redmic.vesselslib.dto.vessel.VesselDTO;
import es.redmic.vesselslib.utils.VesselTrackingUtil;
import es.redmic.vesselslib.utils.VesselUtil;

@Service
public class AISService {

	// @formatter:off

	private static final String INVALID_USERNAME_OR_PASSWORD = "Invalid username or password",
			TOO_FREQUENT_REQUESTS = "Too frequent requests";

	// @formatter:on

	protected static Logger logger = LogManager.getLogger();

	@Value("${aishub.service.url}")
	private String urlAIS;

	@Value("${broker.topic.realtime.tracking.vessels}")
	private String VESSEL_TRACKING_TOPIC;

	@Value("${broker.topic.realtime.ais}")
	private String AIS_TOPIC;

	@Value("${broker.topic.realtime.vessels}")
	private String VESSEL_TOPIC;

	@Value("${file.delimiter.csv}")
	private String delimiterCSV;

	@Value("${property.path.media_storage.AIS_TEMP}")
	private String directoryPath;

	private String nameCompressFile = "ais.zip";
	private String nameFile = "data.csv";

	@Value("${qflag.default}")
	private String QFLAG_DEFAULT;

	@Value("${vflag.default}")
	private String VFLAG_DEFAULT;

	@Value("${vesseltracking-activity-id}")
	protected String activityId;

	@Autowired
	private KafkaTemplate<String, AISTrackingDTO> aisTemplate;

	@Autowired
	private KafkaTemplate<String, CommonDTO> vesselTemplate;

	// @formatter:off
	
	private long maxDateBefore = -1,
			maxDateCurrent = -1;
	
	// @formatter:on

	public void fetchData() {

		prepareFile();
		processFile();
	}

	private void prepareFile() {

		downloadData();
		unzip();
	}

	private void downloadData() {

		try {
			URL url = new URL(urlAIS);
			File destination = new File(getCompressFilePath());
			FileUtils.copyURLToFile(url, destination);
		} catch (IOException e) {
			throw new ResourceNotFoundException();
		}
	}

	private void unzip() {

		Zip zip = new Zip();

		zip.extract(getCompressFilePath(), directoryPath + "/");

		removeZipFile();
	}

	private void processFile() {

		File file = new File(getFilePath());

		DataLoaderIngestData dataLoader = new DataLoaderIngestData(file, delimiterCSV);

		checkFile(dataLoader.getHeader());

		Map<String, String> row;

		while ((row = dataLoader.read()) != null) {
			processRow(row);
		}

		maxDateBefore = maxDateCurrent;
		maxDateCurrent = -1;

		file.delete();
	}

	private void checkFile(List<String> header) {

		if (header.size() == 1) {
			if (header.get(0).contains(INVALID_USERNAME_OR_PASSWORD)) {

				logger.error("Error en el fichero. " + INVALID_USERNAME_OR_PASSWORD);
				throw new InvalidUsernameException();
			} else if (header.get(0).contains(TOO_FREQUENT_REQUESTS)) {

				logger.error("Error en el fichero. " + TOO_FREQUENT_REQUESTS);
				throw new InvalidUsernameException();
			}
		}
	}

	private void processRow(Map<String, String> row) {

		AISTrackingDTO dto = new AISTrackingDTO();

		dto.buildFromMap(row);

		if (dataFulfillConstraints(dto)) {
			publishToKafka(dto);
		}
	}

	private boolean dataFulfillConstraints(AISTrackingDTO dto) {

		if (dto.getMmsi() == null && dto.getTstamp() == null) {
			return false;
		}

		if (dto.getTstamp().getMillis() < maxDateBefore) {
			return false;
		}

		if (dto.getTstamp().getMillis() > maxDateCurrent) {
			maxDateCurrent = dto.getTstamp().getMillis();
		}
		return true;
	}

	private void publishToKafka(AISTrackingDTO aisTracking) {

		// @formatter:off
		String vesselId = VesselUtil.generateId(aisTracking.getMmsi()),
				vesselTrackingId = VesselTrackingUtil.generateId(aisTracking.getMmsi(), aisTracking.getTstamp().getMillis());
		// @formatter:on

		// Envía dto de datos brutos para sink de postgresql
		aisTemplate.send(AIS_TOPIC, vesselId, aisTracking).addCallback(new SendListener());

		VesselTrackingDTO tracking = VesselTrackingUtil.convertTrackToVesselTracking(aisTracking, QFLAG_DEFAULT,
				VFLAG_DEFAULT, activityId);

		// Envía dto de tracking para procesarlo + sink

		vesselTemplate.send(VESSEL_TRACKING_TOPIC, vesselTrackingId, tracking).addCallback(new SendListener());

		VesselDTO vessel = tracking.getProperties().getVessel();

		// Envía dto de vessel para procesarlo
		vesselTemplate.send(VESSEL_TOPIC, vesselId, vessel).addCallback(new SendListener());
	}

	private void removeZipFile() {

		File file = new File(getCompressFilePath());
		file.delete();
	}

	private String getFilePath() {

		return directoryPath + "/" + nameFile;
	}

	private String getCompressFilePath() {

		return directoryPath + "/" + nameCompressFile;
	}
}
