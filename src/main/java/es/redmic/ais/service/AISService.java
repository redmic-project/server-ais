package es.redmic.ais.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import es.redmic.brokerlib.avro.geodata.tracking.vessels.AISTrackingDTO;
import es.redmic.brokerlib.listener.SendListener;
import es.redmic.utils.compressor.Zip;
import es.redmic.utils.csv.DataLoaderIngestData;

@Service
public class AISService {

	protected static Logger logger = LogManager.getLogger();

	@Value("${aishub.service.url}")
	private String urlAIS;

	private String directoryPath = System.getProperty("user.dir") + "/target";
	private String nameCompressFile = "ais.zip";
	private String nameFile = "data.csv";

	private String delimiterCSV = ",";

	@Value("${broker.topic.realtime.tracking.vessels}")
	private String TOPIC;

	@Autowired
	private KafkaTemplate<String, AISTrackingDTO> kafkaTemplate;

	public void update() {

		updateFile();
		processFile();
	}

	public void updateFile() {

		downloadData();
		unzip();
	}

	public void downloadData() {

		try {
			URL url = new URL(getUrlAIS());
			File destination = new File(getCompressFilePath());
			FileUtils.copyURLToFile(url, destination);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void unzip() {

		Zip zip = new Zip();

		zip.extract(getCompressFilePath(), getDirectoryPath() + "/");
	}

	public void processFile() {

		File file = new File(getFilePath());

		DataLoaderIngestData dataLoader = new DataLoaderIngestData(file, getDelimiterCSV());

		Map<String, String> row;

		while ((row = dataLoader.read()) != null) {
			processRow(row);
		}
	}

	public void processRow(Map<String, String> row) {

		AISTrackingDTO dto = new AISTrackingDTO();

		dto.setMmsi(parseInteger(row.get("MMSI")));
		dto.setTstamp(DateTime.parse(row.get("TSTAMP"), DateTimeFormat.forPattern("yyy-MM-dd HH:mm:ss zzz")));
		dto.setLatitude(parseDouble(row.get("LATITUDE")));
		dto.setLongitude(parseDouble(row.get("LONGITUDE")));
		dto.setCog(parseDouble(row.get("COG")));
		dto.setSog(parseDouble(row.get("SOG")));
		dto.setDraught(parseDouble(row.get("DRAUGHT")));
		dto.setType(parseInteger(row.get("TYPE")));
		dto.setImo(parseInteger(row.get("IMO")));
		dto.setHeading(parseInteger(row.get("HEADING")));
		dto.setNavStat(parseInteger(row.get("NAVSTAT")));
		dto.setName(row.get("NAME"));
		dto.setDest(row.get("DEST"));
		dto.setCallSign(row.get("CALLSIGN"));
		dto.setEta(row.get("ETA"));
		dto.setA(parseDouble(row.get("A")));
		dto.setB(parseDouble(row.get("B")));
		dto.setC(parseDouble(row.get("C")));
		dto.setD(parseDouble(row.get("D")));

		emitDTO(dto);
	}

	public Double parseDouble(String value) {

		if (value != null) {
			return Double.parseDouble(value);
		}

		return null;
	}

	public Integer parseInteger(String value) {

		if (value != null) {
			return Integer.parseInt(value);
		}

		return null;
	}

	public void emitDTO(AISTrackingDTO dto) {

		logger.info("Tracking vessel: " + dto.getMmsi());

		ListenableFuture<SendResult<String, AISTrackingDTO>> future = kafkaTemplate.send(TOPIC,
				dto.getMmsi().toString(), dto);

		future.addCallback(new SendListener());
	}

	public String getFilePath() {

		return getDirectoryPath() + "/" + getNameFile();
	}

	public String getCompressFilePath() {

		return getDirectoryPath() + "/" + getNameCompressFile();
	}

	public String getUrlAIS() {
		return urlAIS;
	}

	public String getDirectoryPath() {
		return directoryPath;
	}

	public String getNameCompressFile() {
		return nameCompressFile;
	}

	public String getNameFile() {
		return nameFile;
	}

	public void setNameFile(String nameFile) {
		this.nameFile = nameFile;
	}

	public void setNameCompressFile(String nameCompressFile) {
		this.nameCompressFile = nameCompressFile;
	}

	public void setDirectoryPath(String directoryPath) {
		this.directoryPath = directoryPath;
	}

	public void setUrlAIS(String urlAIS) {
		this.urlAIS = urlAIS;
	}

	public String getDelimiterCSV() {
		return delimiterCSV;
	}

	public void setDelimiterCSV(String delimiterCSV) {
		this.delimiterCSV = delimiterCSV;
	}
}
