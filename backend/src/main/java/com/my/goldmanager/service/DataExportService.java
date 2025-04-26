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

import com.my.goldmanager.service.dataexpimp.DataExporter;
import com.my.goldmanager.service.dataexpimp.ExportDataCryptor;
import com.my.goldmanager.service.entity.ExportData;

/**
 * Exports all Entities from database
 */
@Service
public class DataExportService {

	@Autowired
	private DataExporter dataExporter;

	@Autowired
	private ExportDataCryptor exportDataCryptor;


	/**
	 * Exports the Entities in encrypted and compressed format
	 * 
	 * @param encryptionPassword
	 * @return
	 * @throws Exception
	 */
	public byte[] exportData(String encryptionPassword) throws Exception {
		ExportData exportData = dataExporter.exportData();
		return exportDataCryptor.encrypt(exportData, encryptionPassword);

	}



}
