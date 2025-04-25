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

import java.io.ByteArrayOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.goldmanager.service.dataexpimp.DataExportImportCryptoUtil;
import com.my.goldmanager.service.dataexpimp.DataExporter;
import com.my.goldmanager.service.entity.ExportData;
import com.my.goldmanager.service.exception.ValidationException;

/**
 * Exports all Entities from database
 */
@Component
public class DataExportService {


	private static final byte[] header_start = { 'E', 'x', 'p', 'e', 'n', 'c', 'v', '1' };
	private static final byte[] body_start = { 'E', 'x', 'p', 'd', 'a', 't', 'a', 'v', '1' };
	private static final SecureRandom random = new SecureRandom();

	@Autowired
	private DataExporter dataExporter;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Exports the Entities in encrypted and compressed format
	 * 
	 * @param encryptionPassword
	 * @return
	 * @throws Exception
	 */

	public byte[] exportData(String encryptionPassword) throws Exception {
		if (encryptionPassword == null || encryptionPassword.isBlank()) {
			throw new ValidationException("Encryption password is manadatory.");
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		ExportData exportData = dataExporter.exportData();
		byte[] salt = generateSalt();
		byte[] iv = new byte[16];
		random.nextBytes(iv);

		SecretKey key = DataExportImportCryptoUtil.generateKeyFromPassword(encryptionPassword, salt);
		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
		Cipher cipher = DataExportImportCryptoUtil.getCipher(key, iv, Cipher.ENCRYPT_MODE);

		ByteArrayOutputStream encryptedData = new ByteArrayOutputStream();
		try (CipherOutputStream cout = new CipherOutputStream(encryptedData, cipher)) {
			// Adding magic bytes to ensure correct encryption:
			cout.write(body_start);
			byte[] payload = objectMapper.writeValueAsBytes(exportData);
			cout.write(longToByteArray(payload.length));
			cout.write(payload);
			cout.flush();
		}
		try (DeflaterOutputStream deflaterOutPutStream = new DeflaterOutputStream(bos, deflater)) {
			deflaterOutPutStream.write(header_start);
			deflaterOutPutStream.write(longToByteArray(encryptedData.size()));
			deflaterOutPutStream.write(salt);
			deflaterOutPutStream.write(iv);
			deflaterOutPutStream.write(encryptedData.toByteArray());

			deflaterOutPutStream.flush();
		}

		return bos.toByteArray();

	}

	public static byte[] longToByteArray(long value) {
		byte[] byteArray = new byte[8];
		for (int i = 0; i < 8; i++) {
			byteArray[7 - i] = (byte) (value >> (i * 8));
		}
		return byteArray;
	}

	private static byte[] generateSalt() throws NoSuchAlgorithmException {

		byte[] salt = new byte[16];
		random.nextBytes(salt);
		return salt;
	}
}

