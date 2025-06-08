package com.my.goldmanager.service.dataexpimp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.my.goldmanager.service.entity.ExportData;
import com.my.goldmanager.service.exception.PasswordValidationException;
import com.my.goldmanager.service.exception.ValidationException;

@SpringBootTest
@ActiveProfiles("test")
class ExportDataCryptorTest {

	@Autowired
	private ExportDataCryptor exportDataCryptor;

	@Test
	void testEncryptAndDecrypt_Success() throws Exception {
		ExportData exportData = new ExportData(); // Mock valid ExportData object
		String encryptionPassword = "validPassword";

		byte[] encryptedData = exportDataCryptor.encrypt(exportData, encryptionPassword);
		ExportData decryptedData = exportDataCryptor.decrypt(encryptedData, encryptionPassword);

		assertNotNull(encryptedData);
		assertNotNull(decryptedData);
		// Add assertions to verify decryptedData matches exportData
	}

	@Test
	void testEncrypt_NullData() {
		String encryptionPassword = "validPassword";

		Exception exception = assertThrows(ValidationException.class, () -> {
			exportDataCryptor.encrypt(null, encryptionPassword);
		});

		assertEquals("ExportData cannot be null", exception.getMessage());
	}

	@Test
	void testEncrypt_NullPassword() {
		ExportData exportData = new ExportData();

		PasswordValidationException exception = assertThrows(PasswordValidationException.class, () -> {
			exportDataCryptor.encrypt(exportData, null);
		});

		assertEquals("Password is mandatory and must not contain spaces.", exception.getMessage());
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 7, 8, 9 })
	void testDecrypt_InvalidHeader(int headerLength) throws IOException {
		byte[] invalidHeaderData = createDataWithInvalidHeader(headerLength);
		String encryptionPassword = "validPassword";

		Exception exception = assertThrows(ValidationException.class, () -> {
			exportDataCryptor.decrypt(invalidHeaderData, encryptionPassword);
		});
		if (headerLength < 8) {
			assertEquals("Invalid header length", exception.getMessage());
		} else {
			assertEquals("Invalid header format", exception.getMessage());
		}

	}

	@Test
	void testDecrypt_IncorrectPassword() throws Exception {
		ExportData exportData = new ExportData();
		String correctPassword = "correctPassword";
		String incorrectPassword = "wrongPassword";

		byte[] encryptedData = exportDataCryptor.encrypt(exportData, correctPassword);

		Exception exception = assertThrows(PasswordValidationException.class, () -> {
			exportDataCryptor.decrypt(encryptedData, incorrectPassword);
		});

		assertEquals("Decryption of data has failed, maybe the provided password is incorrect?",
				exception.getMessage());
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 10, 8, 9 })
	void testDecrypt_InvalidBodyHeaderValue(int invalidBodyHeaderLength) throws Exception {
		String encryptionPassword = "validPassword";

		byte[] invalidBodyHeaderData = createDataWithInvalidBodyHeader(encryptionPassword, invalidBodyHeaderLength);

		Exception exception = assertThrows(ValidationException.class, () -> {
			exportDataCryptor.decrypt(invalidBodyHeaderData, encryptionPassword);
		});
		if (invalidBodyHeaderLength < 9) {
			assertEquals("Invalid body header length", exception.getMessage());
		} else {
			assertEquals("Decrypted body can not be verified", exception.getMessage());
		}

	}

	@Test
	void testDecrypt_CorruptedPayload() throws Exception {
		ExportData exportData = new ExportData();
		String encryptionPassword = "validPassword";

		byte[] encryptedData = exportDataCryptor.encrypt(exportData, encryptionPassword);
		// Corrupt the payload
		encryptedData[encryptedData.length - 1] ^= 0xFF;

		Exception exception = assertThrows(ValidationException.class, () -> {
			exportDataCryptor.decrypt(encryptedData, encryptionPassword);
		});

		assertEquals("Corrupted payload", exception.getMessage());
	}

	// Helper method to create data with an invalid header
	private byte[] createDataWithInvalidHeader(int headerLength) throws IOException {
		byte[] invalidHeaderData = new byte[headerLength];

		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
		deflater.setInput(invalidHeaderData);
		deflater.finish();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream, deflater)) {

			deflaterOutputStream.write(invalidHeaderData);
			deflaterOutputStream.finish();
		}
		return outputStream.toByteArray();
	}

    private byte[] createDataWithInvalidBodyHeader(String encryptionPassword, int bodyHeaderLength)
                    throws Exception {
		byte[] invalidBodyHeaderData = new byte[bodyHeaderLength];
		ByteArrayOutputStream bos = new ByteArrayOutputStream();


		byte[] salt = DataExportImportCryptoUtil.generateSalt();
		byte[] iv = DataExportImportCryptoUtil.generateIV();

		SecretKey key = DataExportImportCryptoUtil.generateKeyFromPassword(encryptionPassword, salt);
		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
		Cipher cipher = DataExportImportCryptoUtil.getCipher(key, iv, Cipher.ENCRYPT_MODE);

		ByteArrayOutputStream encryptedData = new ByteArrayOutputStream();
		try (CipherOutputStream cout = new CipherOutputStream(encryptedData, cipher)) {
			// Adding magic bytes to ensure correct encryption:
			cout.write(invalidBodyHeaderData);
			cout.flush();
		}
		try (DeflaterOutputStream deflaterOutPutStream = new DeflaterOutputStream(bos, deflater)) {
			deflaterOutPutStream.write(ExportDataCryptor.header_start);
			byte[] encryptedDataPayload = encryptedData.toByteArray();
			deflaterOutPutStream.write(DataExportImportUtil.longToByteArray(encryptedDataPayload.length));
			deflaterOutPutStream.write(salt);
			deflaterOutPutStream.write(iv);
			deflaterOutPutStream.write(encryptedDataPayload);

			deflaterOutPutStream.flush();
		}

                return bos.toByteArray();
        }

        @Test
        void testDecrypt_TooLargeEncryptedDataSize() throws Exception {
                String encryptionPassword = "validPassword";
                byte[] invalidData = createDataWithLargeEncryptedSize(ExportDataCryptor.MAX_ENCRYPTED_DATA_SIZE + 1);

                Exception exception = assertThrows(ValidationException.class,
                                () -> exportDataCryptor.decrypt(invalidData, encryptionPassword));

                assertEquals("Invalid encrypted data size", exception.getMessage());
        }

        private byte[] createDataWithLargeEncryptedSize(long size) throws IOException {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
                try (DeflaterOutputStream out = new DeflaterOutputStream(bos, deflater)) {
                        out.write(ExportDataCryptor.header_start);
                        out.write(DataExportImportUtil.longToByteArray(size));
                        out.write(new byte[DataExportImportCryptoUtil.SALT_LENGTH]);
                        out.write(new byte[DataExportImportCryptoUtil.IV_LENGTH]);
                        out.flush();
                }
                return bos.toByteArray();
        }

}
