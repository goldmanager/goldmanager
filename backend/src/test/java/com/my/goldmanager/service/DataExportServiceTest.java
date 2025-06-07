package com.my.goldmanager.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.my.goldmanager.service.dataexpimp.DataExporter;
import com.my.goldmanager.service.dataexpimp.ExportDataCryptor;
import com.my.goldmanager.service.entity.ExportData;

@ExtendWith(MockitoExtension.class)
class DataExportServiceTest {

	@InjectMocks
	private DataExportService dataExportService;

	@Mock
	private DataExporter dataExporter;

	@Mock
	private ExportDataCryptor exportDataCryptor;

	@Test
	void testExportData_Success() throws Exception {
		String encryptionPassword = "validPassword";
		ExportData mockExportData = new ExportData();
		byte[] encryptedData = "encryptedData".getBytes();

		Mockito.when(dataExporter.exportData()).thenReturn(mockExportData);
		Mockito.when(exportDataCryptor.encrypt(mockExportData, encryptionPassword)).thenReturn(encryptedData);

		byte[] result = dataExportService.exportData(encryptionPassword);

		assertNotNull(result);
		assertArrayEquals(encryptedData, result);
		Mockito.verify(dataExporter, Mockito.times(1)).exportData();
		Mockito.verify(exportDataCryptor, Mockito.times(1)).encrypt(mockExportData, encryptionPassword);
	}

	@Test
	void testExportData_DataExporterThrowsException() throws Exception {
		String encryptionPassword = "validPassword";

		Mockito.when(dataExporter.exportData()).thenThrow(new RuntimeException("Data export failed"));

		Exception exception = assertThrows(RuntimeException.class, () -> {
			dataExportService.exportData(encryptionPassword);
		});

		assertEquals("Data export failed", exception.getMessage());
		Mockito.verify(dataExporter, Mockito.times(1)).exportData();
		Mockito.verifyNoInteractions(exportDataCryptor);
	}

	@Test
	void testExportData_EncryptionThrowsException() throws Exception {
		String encryptionPassword = "validPassword";
		ExportData mockExportData = new ExportData();

		Mockito.when(dataExporter.exportData()).thenReturn(mockExportData);
		Mockito.when(exportDataCryptor.encrypt(mockExportData, encryptionPassword))
				.thenThrow(new RuntimeException("Encryption failed"));

		Exception exception = assertThrows(RuntimeException.class, () -> {
			dataExportService.exportData(encryptionPassword);
		});

		assertEquals("Encryption failed", exception.getMessage());
		Mockito.verify(dataExporter, Mockito.times(1)).exportData();
		Mockito.verify(exportDataCryptor, Mockito.times(1)).encrypt(mockExportData, encryptionPassword);
	}
}
