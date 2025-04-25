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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class DataExportImportCryptoUtil {
	private static final String ENCRYPTION_ALG = "AES";
	private static final String ENCRYPTION_CIPHER_ALG = "AES/GCM/NoPadding";
	private static final String SECRETKEY_ALG = "PBKDF2WithHmacSHA256";
	private static final int KEY_LENGTH = 256;

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
		// Create a Cipher instance for encryption
		// AES/GCM/NoPadding is used for authenticated encryption
		// with GCM mode and no padding
		// The IV should be 12 bytes for GCM mode
		if (iv.length != 12) {
			throw new IllegalArgumentException("IV must be 12 bytes");
		}
		// Create a Cipher instance for encryption
		Cipher cipher = Cipher.getInstance(ENCRYPTION_CIPHER_ALG);

		GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
		cipher.init(mode, key, gcmSpec);
		return cipher;
	}
}
