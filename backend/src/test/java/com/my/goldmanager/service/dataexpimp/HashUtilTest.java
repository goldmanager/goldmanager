package com.my.goldmanager.service.dataexpimp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.my.goldmanager.service.entity.ExportData;
import com.my.goldmanager.service.exception.InvalidAlgorithmException;
import com.my.goldmanager.service.exception.InvalidHashException;

class HashUtilTest {

	private HashUtil hashUtil;

	@BeforeEach
	void setUp() {
		hashUtil = new HashUtil();
		// Set the default hash algorithm using ReflectionTestUtils
		ReflectionTestUtils.setField(hashUtil, "hashAlgorithm", "sha256");
	}

	@Test
	void testHashData_ValidData() throws InvalidAlgorithmException {
		ExportData data = new ExportData();
		data.setExportEntityData("test data".getBytes());

		hashUtil.hashData(data);

		assertNotNull(data.getHash());
		assertEquals("sha256", data.getHashAlgorithm());
	}

	@Test
	void testValidateHash_ValidData() throws InvalidAlgorithmException, InvalidHashException {
		ExportData data = new ExportData();
		data.setExportEntityData("test data".getBytes());

		hashUtil.hashData(data); // Generate hash
		hashUtil.validateHash(data); // Validate hash

		// If no exception is thrown, the test passes
	}

	@Test
	void testHashData_InvalidAlgorithm() {
		ReflectionTestUtils.setField(hashUtil, "hashAlgorithm", "INVALID");

		ExportData data = new ExportData();
		data.setExportEntityData("test data".getBytes());

		InvalidAlgorithmException exception = assertThrows(InvalidAlgorithmException.class,
				() -> hashUtil.hashData(data));
		assertEquals("Configured hashAlgorithm 'INVALID' is invalid!", exception.getMessage());
	}

	@Test
	void testValidateHash_MissingHashAlgorithm() {
		ExportData data = new ExportData();
		data.setExportEntityData("test data".getBytes());
		data.setHash("dummyHash".getBytes());

		InvalidHashException exception = assertThrows(InvalidHashException.class, () -> hashUtil.validateHash(data));
		assertEquals("Hash algorithm is mandatory.", exception.getMessage());
	}

	@Test
	void testValidateHash_MissingHashValue() {
		ExportData data = new ExportData();
		data.setExportEntityData("test data".getBytes());
		data.setHashAlgorithm("sha256");

		InvalidHashException exception = assertThrows(InvalidHashException.class, () -> hashUtil.validateHash(data));
		assertEquals("Hash value is mandatory.", exception.getMessage());

	}

	@Test
	void testValidateHash_InvalidHashSize() throws InvalidAlgorithmException {
		ExportData data = new ExportData();
		data.setExportEntityData("test data".getBytes());
		hashUtil.hashData(data);

		// Modify the hash to make it invalid in size
		data.setHash(new byte[data.getHash().length - 1]);

		InvalidHashException exception = assertThrows(InvalidHashException.class, () -> hashUtil.validateHash(data));
		assertEquals("Hash value length does not match the expected hash length.", exception.getMessage());
	}
	@Test
	void testValidateHash_InvalidHashValue() throws InvalidAlgorithmException {
		ExportData data = new ExportData();
		data.setExportEntityData("test data".getBytes());
		hashUtil.hashData(data);

		// Modify the hash to make it invalid but keep the same size
		data.setHash(new byte[data.getHash().length]);

		InvalidHashException exception = assertThrows(InvalidHashException.class, () -> hashUtil.validateHash(data));
		assertEquals("Hash value does not match the expected hash value.", exception.getMessage());
	}

	@Test
	void testValidateHash_EmptyData() {
		ExportData data = new ExportData();
		data.setExportEntityData(new byte[0]);
		data.setHashAlgorithm("sha256");
		data.setHash("dummyHash".getBytes());

		InvalidHashException exception = assertThrows(InvalidHashException.class, () -> hashUtil.validateHash(data));
		assertEquals("Data to hash is mandatory.", exception.getMessage());
	}
}
