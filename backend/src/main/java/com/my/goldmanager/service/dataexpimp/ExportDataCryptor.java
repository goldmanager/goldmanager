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
package com.my.goldmanager.service.dataexpimp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.goldmanager.service.PasswordPolicyValidationService;
import com.my.goldmanager.service.entity.ExportData;
import com.my.goldmanager.service.exception.ValidationException;

@Service
public class ExportDataCryptor {

	public static final byte[] header_start = { 'E', 'x', 'p', 'e', 'n', 'c', 'v', '1' };
	public static final byte[] body_start = { 'E', 'x', 'p', 'd', 'a', 't', 'a', 'v', '1' };

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PasswordPolicyValidationService passwordPolicyValidationService;
	/**
	 * Encrypts the ExportData object using the provided encryption password.
	 * 
	 * @param exportData         The ExportData object to be encrypted.
	 * @param encryptionPassword The password used for encryption.
	 * @return The encrypted byte array.
	 * @throws Exception If an error occurs during encryption.
	 */
	public byte[] encrypt(ExportData exportData, String encryptionPassword) throws Exception {
		if (exportData == null) {
			throw new ValidationException("ExportData cannot be null");
		}
		passwordPolicyValidationService.validate(encryptionPassword);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		byte[] salt = DataExportImportCryptoUtil.generateSalt();
		byte[] iv = DataExportImportCryptoUtil.generateIV();

		SecretKey key = DataExportImportCryptoUtil.generateKeyFromPassword(encryptionPassword, salt);
		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
		Cipher cipher = DataExportImportCryptoUtil.getCipher(key, iv, Cipher.ENCRYPT_MODE);

		ByteArrayOutputStream encryptedData = new ByteArrayOutputStream();
		try (CipherOutputStream cout = new CipherOutputStream(encryptedData, cipher)) {
			// Adding magic bytes to ensure correct encryption:
			cout.write(body_start);
			byte[] payload = objectMapper.writeValueAsBytes(exportData);
			cout.write(DataExportImportUtil.longToByteArray(payload.length));
			cout.write(payload);
			cout.flush();
		}
		try (DeflaterOutputStream deflaterOutPutStream = new DeflaterOutputStream(bos, deflater)) {
			deflaterOutPutStream.write(header_start);
			byte[] encryptedDataPayload = encryptedData.toByteArray();
			deflaterOutPutStream.write(DataExportImportUtil.longToByteArray(encryptedDataPayload.length));
			deflaterOutPutStream.write(salt);
			deflaterOutPutStream.write(iv);
			deflaterOutPutStream.write(encryptedDataPayload);

			deflaterOutPutStream.flush();
		}

		return bos.toByteArray();
	}

	/**
	 * Decrypts the given byte array using the provided encryption password.
	 * 
	 * @param data               The byte array to be decrypted.
	 * @param encryptionPassword The password used for decryption.
	 * @return The decrypted ExportData object.
	 * @throws Exception If an error occurs during decryption.
	 */
	public ExportData decrypt(byte[] data, String encryptionPassword) throws Exception {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		try (InflaterInputStream inflaterInputStream = new InflaterInputStream(bis)) {

			byte[] header = new byte[header_start.length];
			int headerlength = inflaterInputStream.read(header);
			if (headerlength != header_start.length) {
				throw new ValidationException("Invalid header length");
			}
			if (Arrays.equals(header, header_start) == false) {
				throw new ValidationException("Invalid header format");
			}

                        byte[] encryptedDataSizeBytes = new byte[8];
                        DataExportImportUtil.readFully(inflaterInputStream, encryptedDataSizeBytes);
			long encryptedDataSize = DataExportImportUtil.byteArrayToLong(encryptedDataSizeBytes);

                        byte[] salt = new byte[16];
                        DataExportImportUtil.readFully(inflaterInputStream, salt);

                        byte[] iv = new byte[DataExportImportCryptoUtil.IV_LENGTH];
                        DataExportImportUtil.readFully(inflaterInputStream, iv);

                        byte[] encryptedData = new byte[(int) encryptedDataSize];
                        DataExportImportUtil.readFully(inflaterInputStream, encryptedData);

			SecretKey key = DataExportImportCryptoUtil.generateKeyFromPassword(encryptionPassword, salt);
			Cipher cipher = DataExportImportCryptoUtil.getCipher(key, iv, Cipher.DECRYPT_MODE);

			ByteArrayInputStream encryptedDataStream = new ByteArrayInputStream(encryptedData);
			try (CipherInputStream cipherInputStream = new CipherInputStream(encryptedDataStream, cipher)) {

				byte[] bodyStart = new byte[body_start.length];
				int bodyHeaderLength = cipherInputStream.read(bodyStart);
				if (bodyHeaderLength != body_start.length) {
					throw new ValidationException("Invalid body header length");
				}
				if (!Arrays.equals(bodyStart, body_start)) {
					throw new ValidationException(
							"Decrypted body can not be verified");
				}

                                byte[] payloadSizeBytes = new byte[8];
                                DataExportImportUtil.readFully(cipherInputStream, payloadSizeBytes);

				long payloadSize = DataExportImportUtil.byteArrayToLong(payloadSizeBytes);

                                byte[] payload = new byte[(int) payloadSize];
                                DataExportImportUtil.readFully(cipherInputStream, payload);

				return objectMapper.readValue(payload, ExportData.class);

			} catch (IOException ioex) {
				throw new ValidationException(
						"Decryption of data has failed, maybe the provided password is incorrect?", ioex);
			}
		} catch (ZipException e) {
			throw new ValidationException("Corrupted payload", e);
		}
	}

}
