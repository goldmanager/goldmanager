package com.my.goldmanager.service.dataexpimp;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

import com.my.goldmanager.service.exception.PasswordValidationException;

class DataExportImportCryptoUtilTest {

	@Test
	void testGenerateKeyFromPasswordWithNullOrEmptyPassword() throws NoSuchAlgorithmException {
		byte[] salt = DataExportImportCryptoUtil.generateSalt();

		// Test with null password
		Exception nullPasswordException = assertThrows(PasswordValidationException.class, () -> {
			DataExportImportCryptoUtil.generateKeyFromPassword(null, salt);
		});
		assertTrue(nullPasswordException.getMessage().contains("Password must not be null or empty"),
				"Exception message should indicate that the password is invalid");

		// Test with empty password
		Exception emptyPasswordException = assertThrows(PasswordValidationException.class, () -> {
			DataExportImportCryptoUtil.generateKeyFromPassword("", salt);
		});
		assertTrue(emptyPasswordException.getMessage().contains("Password must not be null or empty"),
				"Exception message should indicate that the password is invalid");
	}

	@Test
	void testGenerateSalt() throws NoSuchAlgorithmException {
		byte[] salt1 = DataExportImportCryptoUtil.generateSalt();
		byte[] salt2 = DataExportImportCryptoUtil.generateSalt();

		assertNotNull(salt1, "Generated salt should not be null");
		assertNotNull(salt2, "Generated salt should not be null");
		assertEquals(DataExportImportCryptoUtil.SALT_LENGTH, salt1.length,
				"Salt length should match the defined length");
		assertEquals(DataExportImportCryptoUtil.SALT_LENGTH, salt2.length,
				"Salt length should match the defined length");
		assertNotEquals(new String(salt1), new String(salt2), "Generated salts should be random");
	}

	@Test
	void testGenerateIV() {
		byte[] iv1 = DataExportImportCryptoUtil.generateIV();
		byte[] iv2 = DataExportImportCryptoUtil.generateIV();

		assertNotNull(iv1, "Generated IV should not be null");
		assertNotNull(iv2, "Generated IV should not be null");
		assertEquals(DataExportImportCryptoUtil.IV_LENGTH, iv1.length, "IV length should match the defined length");
		assertEquals(DataExportImportCryptoUtil.IV_LENGTH, iv2.length, "IV length should match the defined length");
		assertNotEquals(new String(iv1), new String(iv2), "Generated IVs should be random");
	}

	@Test
	void testGenerateKeyFromPassword() throws Exception {
		String password = "testPassword";
		byte[] salt = DataExportImportCryptoUtil.generateSalt();

		SecretKey key = DataExportImportCryptoUtil.generateKeyFromPassword(password, salt);

		assertNotNull(key, "Generated key should not be null");
		assertEquals(DataExportImportCryptoUtil.KEY_LENGTH / 8, key.getEncoded().length,
				"Key length should match the defined length");
	}

	@Test
	void testGenerateKeyFromPasswordWithInvalidSalt() {
		String password = "testPassword";
		byte[] invalidSalt = new byte[8]; // Invalid salt length

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			DataExportImportCryptoUtil.generateKeyFromPassword(password, invalidSalt);
		});

		assertTrue(exception.getMessage().contains("Salt length must be"),
				"Exception message should indicate invalid salt length");
	}

	@Test
	void testGetCipher() throws Exception {
		String password = "testPassword";
		byte[] salt = DataExportImportCryptoUtil.generateSalt();
		byte[] iv = DataExportImportCryptoUtil.generateIV();
		SecretKey key = DataExportImportCryptoUtil.generateKeyFromPassword(password, salt);

		Cipher cipher = DataExportImportCryptoUtil.getCipher(key, iv, Cipher.ENCRYPT_MODE);

		assertNotNull(cipher, "Cipher instance should not be null");
	}

	@Test
	void testGetCipherWithInvalidIV() throws Exception {
		String password = "testPassword";
		byte[] salt = DataExportImportCryptoUtil.generateSalt();
		byte[] invalidIV = new byte[16]; // Invalid IV length
		SecretKey key = DataExportImportCryptoUtil.generateKeyFromPassword(password, salt);

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			DataExportImportCryptoUtil.getCipher(key, invalidIV, Cipher.ENCRYPT_MODE);
		});

		assertTrue(exception.getMessage().contains("IV length must be"),
				"Exception message should indicate invalid IV length");
	}

	@Test
	void testGetCipherWithInvalidKey() {
		byte[] invalidKey = new byte[16]; // Invalid key length
		byte[] iv = DataExportImportCryptoUtil.generateIV();

		SecretKey invalidSecretKey = new SecretKeySpec(invalidKey, DataExportImportCryptoUtil.ENCRYPTION_ALG);

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			DataExportImportCryptoUtil.getCipher(invalidSecretKey, iv, Cipher.ENCRYPT_MODE);
		});

		assertTrue(exception.getMessage().contains("Key length must be"),
				"Exception message should indicate invalid key length");
	}

	@Test
	void testGetCipherWithNullIV() throws Exception {
		String password = "testPassword";
		byte[] salt = DataExportImportCryptoUtil.generateSalt();
		SecretKey key = DataExportImportCryptoUtil.generateKeyFromPassword(password, salt);

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			DataExportImportCryptoUtil.getCipher(key, null, Cipher.ENCRYPT_MODE);
		});

		assertTrue(exception.getMessage().contains("IV must not be null"),
				"Exception message should indicate that the IV is invalid");
	}

	@Test
	void testGetCipherWithNullKey() throws Exception {

		byte[] iv = DataExportImportCryptoUtil.generateIV();

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			DataExportImportCryptoUtil.getCipher(null, iv, Cipher.ENCRYPT_MODE);
		});

		assertTrue(exception.getMessage().contains("Key must not be null"),
				"Exception message should indicate that the key is invalid");
	}

	@Test
	void testGenerateKeyFromPasswordWithExcessivelyLargePassword() throws Exception {
		byte[] salt = DataExportImportCryptoUtil.generateSalt();
		String largePassword = "a".repeat(10000); // Very large password

		SecretKey key = DataExportImportCryptoUtil.generateKeyFromPassword(largePassword, salt);

		assertNotNull(key, "Generated key should not be null for large password");
	}

	@Test
	void testGenerateKeyFromPasswordWithExcessivelyLargeSalt() {
		String password = "testPassword";
		byte[] largeSalt = new byte[10000]; // Very large salt

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			DataExportImportCryptoUtil.generateKeyFromPassword(password, largeSalt);
		});

		assertTrue(exception.getMessage().contains("Salt length must be"),
				"Exception message should indicate invalid salt length");
	}

}
