package es.redmic.test.ais.unit.service;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import es.redmic.ais.service.AISService;

public class AISServiceTest {

	AISService aisService;

	@Before
	public void setup() throws IllegalArgumentException, IllegalAccessException {

		aisService = new AISService();

		aisService.setDirectoryPath(System.getProperty("user.dir") + "/src/test/resources/");
	}

	@Test
	public void shouldDownloadFile() throws Exception {

		File file = new File(aisService.getCompressFilePath());

		if (file.exists()) {
			file.delete();
		}

		aisService.downloadData();

		assertTrue(file.exists());

		if (file.exists()) {
			file.delete();
		}
	}

	@Test
	public void shouldUnzipFile() throws Exception {

		aisService.setNameCompressFile("data.zip");
		aisService.unzip();

		File file = new File(aisService.getFilePath());

		assertTrue(file.exists());

		if (file.exists()) {
			file.delete();
		}
	}
}
