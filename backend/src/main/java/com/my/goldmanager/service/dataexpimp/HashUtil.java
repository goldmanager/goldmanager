package com.my.goldmanager.service.dataexpimp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.my.goldmanager.service.entity.ExportData;
import com.my.goldmanager.service.exception.InvalidAlgorithmException;
import com.my.goldmanager.service.exception.InvalidHashException;

/**
 * Copyright 2025 fg12111
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * 
 */
@Component
class HashUtil {
	@Value("${com.my.goldmanager.service.dataexpimp.hashalgorithm:sha256}")
	private String hashAlgorithm;

	private byte[] generateHash(byte[] data, String hashAlgorithm) throws InvalidAlgorithmException {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(hashAlgorithm);
			return digest.digest(data);
		} catch (NoSuchAlgorithmException e) {
			throw new InvalidAlgorithmException("Configured hashAlgorithm '" + hashAlgorithm + "' is invalid!", e);
		}

	}

	/**
	 * Generates a hash of the given data using the configured hash algorithm.
	 * 
	 * @param data
	 * @return
	 * @throws InvalidAlgorithmException
	 */
	public void hashData(ExportData data) throws InvalidAlgorithmException {
		data.setHash(generateHash(data.getExportEntityData(), hashAlgorithm));
		data.setHashAlgorithm(hashAlgorithm);
	}

	/**
	 * Validates the hash of the given data using the specified hash algorithm.
	 * 
	 * @param data
	 * @throws InvalidAlgorithmException
	 * @throws InvalidHashException
	 */
	public void validateHash(ExportData data) throws InvalidAlgorithmException, InvalidHashException {
		if (data.getHashAlgorithm() == null || data.getHashAlgorithm().isBlank()) {
			throw new InvalidHashException("Hash algorithm is mandatory.");
		}
		if (data.getHash() == null || data.getHash().length == 0) {
			throw new InvalidHashException("Hash value is mandatory.");
		}
		if (data.getExportEntityData() == null || data.getExportEntityData().length == 0) {
			throw new InvalidHashException("Data to hash is mandatory.");
		}

		byte[] generatedHash = generateHash(data.getExportEntityData(), data.getHashAlgorithm());
		if (generatedHash.length != data.getHash().length) {
			throw new InvalidHashException("Hash value length does not match the expected hash length.");
		}
		if (!Arrays.areEqual(generatedHash, data.getHash())) {
			throw new InvalidHashException("Hash value does not match the expected hash value.");
		}


	}

}
