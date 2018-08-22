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
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import es.redmic.ais.exceptions.InvalidUsernameException;
import es.redmic.brokerlib.avro.geodata.tracking.vessels.AISTrackingDTO;
import es.redmic.brokerlib.listener.SendListener;
import es.redmic.exception.custom.ResourceNotFoundException;
import es.redmic.utils.compressor.Zip;
import es.redmic.utils.csv.DataLoaderIngestData;

@Service
public class AISService {

	// @formatter:off

	private static final String INVALID_USERNAME_OR_PASSWORD = "Invalid username or password",
			TOO_FREQUENT_REQUESTS = "Too frequent requests";

	// @formatter:on

	protected static Logger logger = LogManager.getLogger();

	@Value("${broker.topic.realtime.tracking.vessels.key.prefix}")
	private String prefix;

	@Value("${aishub.service.url}")
	private String urlAIS;

	@Value("${broker.topic.realtime.tracking.vessels}")
	private String TOPIC;

	@Value("${file.delimiter.csv}")
	private String delimiterCSV;

	@Value("${property.path.media_storage.AIS_TEMP}")
	private String directoryPath;

	private String nameCompressFile = "ais.zip";
	private String nameFile = "data.csv";

	@Autowired
	private KafkaTemplate<String, AISTrackingDTO> kafkaTemplate;

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

		publishToKafka(dto);
	}

	private void publishToKafka(AISTrackingDTO dto) {

		logger.info("Tracking vessel: " + dto.getMmsi());

		ListenableFuture<SendResult<String, AISTrackingDTO>> future = kafkaTemplate.send(TOPIC,
				prefix + dto.getMmsi().toString(), dto);

		future.addCallback(new SendListener());
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
