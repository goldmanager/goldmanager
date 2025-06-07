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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.my.goldmanager.service.exception.PasswordValidationException;

public class DataExportImportCryptoUtil {
	public static final int SALT_LENGTH = 16;
	public static final String ENCRYPTION_ALG = "AES";
	public static final String ENCRYPTION_CIPHER_ALG = "AES/GCM/NoPadding";
	public static final String SECRETKEY_ALG = "PBKDF2WithHmacSHA256";
	public static final int KEY_LENGTH = 256;
	public static final int IV_LENGTH = 12; // 96 bits

	private static final SecureRandom random = new SecureRandom();
	private DataExportImportCryptoUtil() {
		// Prevent instantiation
	}

	/**
	 * Generates a random key for AES encryption.
	 * 
	 * @param password
	 * @param salt
	 * @return
	 * @throws Exception
	 */
	public static SecretKey generateKeyFromPassword(String password, byte[] salt) throws Exception {

		if (password == null || password.isEmpty()) {
			throw new PasswordValidationException("Password must not be null or empty");
		}
		if (salt.length != SALT_LENGTH) {
			throw new IllegalArgumentException("Salt length must be " + SALT_LENGTH + " bytes");
		}
		int iterations = 65536;

		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH);
		SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRETKEY_ALG);
		byte[] keyBytes = factory.generateSecret(spec).getEncoded();

		return new SecretKeySpec(keyBytes, ENCRYPTION_ALG);
	}

	/**
	 * Generates Cipher instance for encryption or decryption.
	 * 
	 * @return
	 */
	public static Cipher getCipher(SecretKey key, byte[] iv, int mode) throws Exception {

		if (iv == null) {
			throw new IllegalArgumentException("IV must not be null");
		}
		if (iv.length > IV_LENGTH) {
			throw new IllegalArgumentException("IV length must be " + IV_LENGTH + " bytes");
		}
		if (key == null) {
			throw new IllegalArgumentException("Key must not be null");
		}
		if (key.getEncoded().length != KEY_LENGTH / 8) {
			throw new IllegalArgumentException("Key length must be " + KEY_LENGTH / 8 + " bytes");
		}

		Cipher cipher = Cipher.getInstance(ENCRYPTION_CIPHER_ALG);

		GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
		cipher.init(mode, key, gcmSpec);
		return cipher;
	}

	/**
	 * Generates a random salt for key generation.
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] generateSalt() throws NoSuchAlgorithmException {

		byte[] salt = new byte[SALT_LENGTH];
		random.nextBytes(salt);
		return salt;
	}

	/**
	 * Generates a random IV for AES encryption.
	 * 
	 * @return
	 */
	public static byte[] generateIV() {
		byte[] iv = new byte[IV_LENGTH];
		random.nextBytes(iv);
		return iv;
	}
}
