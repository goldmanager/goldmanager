package com.my.goldmanager.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.zafarkhaja.semver.Version;
import com.my.goldmanager.service.exception.VersionLoadingException;

@SpringBootTest
@ActiveProfiles("test")
public class VersionInfoServiceTest {

	@Autowired
	private VersionInfoService versionInfoService;


	/**
	 * Tests if the {@link VersionInfoService} returns the real application Version
	 * 
	 * @throws IOException
	 * @throws VersionLoadingException
	 */
	@Test
	void testRealVersion() throws IOException, VersionLoadingException {
		Properties properties = loadProperties("/version.properties");
		String versionString = properties.getProperty("version");
		Version expected = Version.parse(versionString);
		Assertions.assertNotNull(expected);
		Version actual = versionInfoService.getVersion();
		Assertions.assertNotNull(actual);

		Assertions.assertEquals(expected.toString(), actual.toString());
	}


	/**
	 * Tests if the {@link VersionInfoService} returns a version from a test
	 * properties file
	 * 
	 * @throws IOException
	 * @throws VersionLoadingException
	 */
	@Test
	void testTestPropertiesVersion() throws IOException, VersionLoadingException {
		Properties properties = loadProperties("testversion.properties");
		String versionString = properties.getProperty("version");
		Version expected = Version.parse(versionString);
		Assertions.assertNotNull(expected);
		Version actual = new VersionInfoService("testversion.properties").getVersion();
		Assertions.assertNotNull(actual);

		Assertions.assertEquals(expected.toString(), actual.toString());
	}

	@Test
	void testMissingPropertiesFile() {
		VersionLoadingException exception = Assertions.assertThrows(VersionLoadingException.class, () -> {
			new VersionInfoService("nonexistent.properties").getVersion();
		});

		Assertions.assertEquals(
				"Loading of version properties has failed, maybe the version properties file was not properly generated.",
				exception.getMessage());
	}

	/**
	 * Tests version property not existing in properties file.
	 */
	@Test
	void testVersionPropertyNotExisting() {
		VersionLoadingException exception = Assertions.assertThrows(VersionLoadingException.class, () -> {
			new VersionInfoService("nonexistantVersion.properties").getVersion();
		});
		Assertions.assertEquals(
				"Version property is missing or empty inthe provided version properties file.",
				exception.getMessage());
	}

	/**
	 * Tests invalid version String "abcd"
	 */
	@Test
	void testInvalidVersionProperty() {
		VersionLoadingException exception = Assertions.assertThrows(VersionLoadingException.class, () -> {
			new VersionInfoService("invalidVersion.properties").getVersion();
		});
		Assertions.assertEquals(
				"Loading of version properties has failed, the read version is provided in an unexpected format.",
				exception.getMessage());
	}

	private Properties loadProperties(String propertiesFile) throws IOException {
		try (InputStream in = VersionInfoServiceTest.class.getResourceAsStream(propertiesFile)) {
			Properties properties = new Properties();
			properties.load(in);
			return properties;
		}
	}
}
