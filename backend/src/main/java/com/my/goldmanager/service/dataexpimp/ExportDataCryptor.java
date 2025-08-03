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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.goldmanager.service.PasswordPolicyValidationService;
import com.my.goldmanager.service.entity.ExportData;
import com.my.goldmanager.service.exception.PasswordValidationException;
import com.my.goldmanager.service.exception.ValidationException;

@Service
public class ExportDataCryptor {

	public static final byte[] header_start = { 'E', 'x', 'p', 'e', 'n', 'c', 'v', '1' };
	public static final byte[] body_start = { 'E', 'x', 'p', 'd', 'a', 't', 'a', 'v', '1' };

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PasswordPolicyValidationService passwordPolicyValidationService;

	@Value("${com.my.goldmanager.service.dataexpimp.maxEncryptedDataSize:52428800}")
	private long maxEncryptedDataSize;

	public long getMaxEncryptedDataSize() {
		return maxEncryptedDataSize;
	}

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
		try {
			passwordPolicyValidationService.validate(encryptionPassword);
		} catch (ValidationException ve) {
			throw new PasswordValidationException(ve.getMessage(), ve);
		}

                byte[] salt = DataExportImportCryptoUtil.generateSalt();
                byte[] iv = DataExportImportCryptoUtil.generateIV();

                SecretKey key = DataExportImportCryptoUtil.generateKeyFromPassword(encryptionPassword, salt);
                Cipher cipher = DataExportImportCryptoUtil.getCipher(key, iv, Cipher.ENCRYPT_MODE);

                byte[] payload = objectMapper.writeValueAsBytes(exportData);
                ByteArrayOutputStream compressedPayload = new ByteArrayOutputStream();
                try (DeflaterOutputStream deflaterStream =
                                new DeflaterOutputStream(compressedPayload, new Deflater(Deflater.BEST_COMPRESSION))) {
                        deflaterStream.write(payload);
                }
                byte[] compressedBytes = compressedPayload.toByteArray();

                ByteArrayOutputStream encryptedData = new ByteArrayOutputStream();
                try (CipherOutputStream cout = new CipherOutputStream(encryptedData, cipher)) {
                        // Adding magic bytes to ensure correct encryption:
                        cout.write(body_start);
                        cout.write(DataExportImportUtil.longToByteArray(compressedBytes.length));
                        cout.write(compressedBytes);
                        cout.flush();
                }

                ByteArrayOutputStream bos = new ByteArrayOutputStream(
                                header_start.length + 8 + salt.length + iv.length + encryptedData.size());
                bos.write(header_start);
                byte[] encryptedDataPayload = encryptedData.toByteArray();
                bos.write(DataExportImportUtil.longToByteArray(encryptedDataPayload.length));
                bos.write(salt);
                bos.write(iv);
                bos.write(encryptedDataPayload);
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
                java.io.InputStream dataStream;
                bis.mark(header_start.length);
                byte[] possibleHeader = bis.readNBytes(header_start.length);
                bis.reset();
                if (Arrays.equals(possibleHeader, header_start)) {
                        dataStream = bis;
                } else {
                        dataStream = new InflaterInputStream(bis);
                }
                try {

                        byte[] header = new byte[header_start.length];
                        int headerlength = dataStream.read(header);
                        if (headerlength != header_start.length) {
                                throw new ValidationException("Invalid header length");
                        }
                        if (!Arrays.equals(header, header_start)) {
                                throw new ValidationException("Invalid header format");
                        }

                        byte[] encryptedDataSizeBytes = new byte[8];
                        DataExportImportUtil.readFully(dataStream, encryptedDataSizeBytes);
                        long encryptedDataSize = DataExportImportUtil.byteArrayToLong(encryptedDataSizeBytes);

                        if (encryptedDataSize > maxEncryptedDataSize || encryptedDataSize > Integer.MAX_VALUE) {
                                throw new ValidationException("Encrypted payload exceeds configured maximum size");
                        }

                        byte[] salt = new byte[16];
                        DataExportImportUtil.readFully(dataStream, salt);

                        byte[] iv = new byte[DataExportImportCryptoUtil.IV_LENGTH];
                        DataExportImportUtil.readFully(dataStream, iv);

                        byte[] encryptedData = new byte[(int) encryptedDataSize];
                        DataExportImportUtil.readFully(dataStream, encryptedData);

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
                                        throw new ValidationException("Decrypted body can not be verified");
                                }

                                byte[] payloadSizeBytes = new byte[8];
                                DataExportImportUtil.readFully(cipherInputStream, payloadSizeBytes);

                                long payloadSize = DataExportImportUtil.byteArrayToLong(payloadSizeBytes);

                                if (payloadSize <= 0 || payloadSize > maxEncryptedDataSize || payloadSize > Integer.MAX_VALUE) {
                                        throw new ValidationException("Decrypted payload exceeds configured maximum size");
                                }

                                byte[] payload = new byte[(int) payloadSize];
                                DataExportImportUtil.readFully(cipherInputStream, payload);

                                byte[] jsonBytes;
                                try (InflaterInputStream payloadInflater =
                                                new InflaterInputStream(new ByteArrayInputStream(payload))) {
                                        jsonBytes = payloadInflater.readAllBytes();
                                } catch (ZipException zipEx) {
                                        jsonBytes = payload;
                                }

                                return objectMapper.readValue(jsonBytes, ExportData.class);

                        } catch (IOException ioex) {
                                throw new PasswordValidationException(
                                                "Decryption of data has failed, maybe the provided password is incorrect?", ioex);
                        }
                } catch (ZipException e) {
                        throw new ValidationException("Corrupted payload", e);
                }
        }

}
