package com.my.goldmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.zip.InflaterInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.goldmanager.service.dataexpimp.DataExportImportCryptoUtil;
import com.my.goldmanager.service.dataexpimp.DataExportImportUtil;
import com.my.goldmanager.service.entity.ExportData;
import com.my.goldmanager.service.exception.ValidationException;

@SpringBootTest
@ActiveProfiles("test")
class DataExporterServiceSpringBootTest {

	@Autowired
	private DataExportService dataExportService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void testExportDataStructureAndDecryption() throws Exception {
		String encryptionPassword = "securePassword123";

		// Export data
		byte[] exportedData = dataExportService.exportData(encryptionPassword);
		assertNotNull(exportedData, "Exported data should not be null");
		assertTrue(exportedData.length > 0, "Exported data should not be empty");

		// Verify structure and decrypt
		ByteArrayInputStream bis = new ByteArrayInputStream(exportedData);
		try (InflaterInputStream inflaterInputStream = new InflaterInputStream(bis)) {

			byte[] header = new byte[DataExportService.header_start.length];
			assertEquals(inflaterInputStream.read(header), header.length, "Header length mismatch");
			assertTrue(Arrays.equals(header, DataExportService.header_start), "Header does not match");

			byte[] encryptedDataSizeBytes = new byte[8];
			inflaterInputStream.read(encryptedDataSizeBytes);
			long encryptedDataSize = DataExportImportUtil.byteArrayToLong(encryptedDataSizeBytes);

			byte[] salt = new byte[16];
			inflaterInputStream.read(salt);

			byte[] iv = new byte[DataExportImportCryptoUtil.IV_LENGTH];
			inflaterInputStream.read(iv);

			byte[] encryptedData = new byte[(int) encryptedDataSize];
			inflaterInputStream.read(encryptedData);

			SecretKey key = DataExportImportCryptoUtil.generateKeyFromPassword(encryptionPassword, salt);
			Cipher cipher = DataExportImportCryptoUtil.getCipher(key, iv, Cipher.DECRYPT_MODE);

			ByteArrayInputStream encryptedDataStream = new ByteArrayInputStream(encryptedData);
			try (CipherInputStream cipherInputStream = new CipherInputStream(encryptedDataStream, cipher)) {

				byte[] bodyStart = new byte[DataExportService.body_start.length];
				cipherInputStream.read(bodyStart);
				assertTrue(Arrays.equals(bodyStart, DataExportService.body_start), "Body start does not match");

				byte[] payloadSizeBytes = new byte[8];
				cipherInputStream.read(payloadSizeBytes);
				long payloadSize = DataExportImportUtil.byteArrayToLong(payloadSizeBytes);

				byte[] payload = new byte[(int) payloadSize];
				cipherInputStream.read(payload);

				ExportData exportData = objectMapper.readValue(payload, ExportData.class);
				assertNotNull(exportData, "Decrypted export data should not be null");
			}
		}

	}

	@Test
	void testExportDataWithEmptyPassword() {
		String emptyPassword = "";

		Exception exception = assertThrows(ValidationException.class, () -> {
			dataExportService.exportData(emptyPassword);
		});

		assertEquals("Encryption password is mandatory.", exception.getMessage());
	}

	@Test
	void testExportDataWithNullPassword() {
		String nullPassword = null;

		Exception exception = assertThrows(ValidationException.class, () -> {
			dataExportService.exportData(nullPassword);
		});

		assertEquals("Encryption password is mandatory.", exception.getMessage());
	}
}
