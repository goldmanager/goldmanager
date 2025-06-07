/** Copyright 2025 fg12111

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
 * 
 */
package com.my.goldmanager.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.stereotype.Component;

import com.github.zafarkhaja.semver.UnexpectedCharacterException;
import com.github.zafarkhaja.semver.Version;
import com.my.goldmanager.service.exception.VersionLoadingException;

/**
 * VersionInfo service
 */
@Component
public class VersionInfoService {

	private static final String DEFAULT_PROPERTIES_FILE = "/version.properties";

	private final String propertiesFile;
	private volatile Version version = null;

	public VersionInfoService() {
		this(DEFAULT_PROPERTIES_FILE);
	}

	/**
	 * Constructor for junit testing. The visibility is intended.
	 * 
	 * @param propertiesFile
	 */
	VersionInfoService(String propertiesFile) {
		this.propertiesFile = propertiesFile;
	}

	/**
	 * Reads the application version from the `version.properties` file located in
	 * the classpath. The `version.properties` file must contain a property named
	 * `version`.
	 * 
	 * @return the current version of the application
	 * @throws VersionLoadingException
	 */
	public Version getVersion() throws VersionLoadingException {
		if (version == null) {
			synchronized (this) {
				if (version == null) {
					try (InputStream in = VersionInfoService.class.getResourceAsStream(propertiesFile)) {
						Properties properties = loadProperties(in);
						String versionString = properties.getProperty("version");
						if (versionString == null || versionString.isBlank()) {
							throw new VersionLoadingException(
									"Version property is missing or empty inthe provided version properties file.");
						}
						version = Version.parse(versionString);
					} catch (IOException e) {
						throw new VersionLoadingException(
								"Loading of version properties has failed, maybe the version properties file was not properly generated.",
								e);
					}
					catch (UnexpectedCharacterException uce) {
						throw new VersionLoadingException(
								"Loading of version properties has failed, the read version is provided in an unexpected format.",
								uce);
					}
				}
			}
		}
		return version;
	}

	private Properties loadProperties(InputStream in) throws IOException {
		if (in == null) {
			throw new IOException("Fatal: version.properties not found!");
		}
		Properties properties = new Properties();
		properties.load(in);
		return properties;
	}
}
