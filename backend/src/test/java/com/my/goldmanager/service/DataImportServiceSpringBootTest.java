package com.my.goldmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.my.goldmanager.service.dataexpimp.DataImporter;
import com.my.goldmanager.service.dataexpimp.ExportDataCryptor;
import com.my.goldmanager.service.entity.ExportData;
import com.my.goldmanager.service.exception.PasswordValidationException;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class DataImportServiceSpringBootTest {

	@Autowired
	private DataImportService dataImportService;

	@MockitoBean
	private DataImporter dataImporter;

	@MockitoBean
	private ExportDataCryptor exportDataCryptor;

	@Test
	void testImportData_Success() throws Exception {
		byte[] validData = "validData".getBytes();
		String encryptionPassword = "validPassword";

		ExportData mockExportData = new ExportData();
		Mockito.when(exportDataCryptor.decrypt(validData, encryptionPassword)).thenReturn(mockExportData);

		dataImportService.importData(validData, encryptionPassword);

		Mockito.verify(dataImporter, Mockito.times(1)).importData(mockExportData);
	}

	@Test
	void testImportData_NullData() {
		String encryptionPassword = "validPassword";

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			dataImportService.importData(null, encryptionPassword);
		});

		assertEquals("Data cannot be null or empty", exception.getMessage());
	}

	@Test
	void testImportData_EmptyData() {
		byte[] emptyData = new byte[0];
		String encryptionPassword = "validPassword";

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			dataImportService.importData(emptyData, encryptionPassword);
		});

		assertEquals("Data cannot be null or empty", exception.getMessage());
	}

	@Test
	void testImportData_NullPassword() {
		byte[] validData = "validData".getBytes();

		Exception exception = assertThrows(PasswordValidationException.class, () -> {
			dataImportService.importData(validData, null);
		});

		assertEquals("Encryption password cannot be null or empty", exception.getMessage());
	}

	@Test
	void testImportData_EmptyPassword() {
		byte[] validData = "validData".getBytes();
		String emptyPassword = "";

		Exception exception = assertThrows(PasswordValidationException.class, () -> {
			dataImportService.importData(validData, emptyPassword);
		});

		assertEquals("Encryption password cannot be null or empty", exception.getMessage());
	}

	@Test
	void testImportData_DecryptionFailure() throws Exception {
		byte[] validData = "validData".getBytes();
		String encryptionPassword = "validPassword";

		Mockito.when(exportDataCryptor.decrypt(validData, encryptionPassword)).thenReturn(null);

		Exception exception = assertThrows(PasswordValidationException.class, () -> {
			dataImportService.importData(validData, encryptionPassword);
		});

		assertEquals("Reading of decrypted data failed, maybe the provided password is incorrect?",
				exception.getMessage());
	}
}
