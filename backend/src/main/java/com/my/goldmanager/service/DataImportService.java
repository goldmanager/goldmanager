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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.my.goldmanager.service.dataexpimp.DataImporter;
import com.my.goldmanager.service.dataexpimp.ExportDataCryptor;
import com.my.goldmanager.service.entity.ExportData;
import com.my.goldmanager.service.exception.PasswordValidationException;

/**
 * Imports all data from supplied data into database
 */
@Service
public class DataImportService {

	@Autowired
	private DataImporter dataImporter;
	@Autowired
	private ExportDataCryptor exportDataCryptor;

	/**
	 * Imports the data from the supplied byte array
	 * 
	 * @param data               the byte array containing the data to be imported
	 * @param encryptionPassword the password used for encryption of the import data
	 * @throws Exception
	 * @throws IllegalArgumentException
	 */
	public void importData(byte[] data, String encryptionPassword) throws Exception {

		if (data == null || data.length == 0) {
			throw new IllegalArgumentException("Data cannot be null or empty");
		}
		if (encryptionPassword == null || encryptionPassword.isEmpty()) {
			throw new PasswordValidationException("Encryption password cannot be null or empty");
		}
		ExportData exportData = exportDataCryptor.decrypt(data, encryptionPassword);
		if (exportData == null) {
			throw new PasswordValidationException(
					"Reading of decrypted data failed, maybe the provided password is incorrect?");
		}
		dataImporter.importData(exportData);
	}
}
