package com.my.goldmanager.service.dataexpimp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class DataExportImportUtilTest {

	@ParameterizedTest
	@ValueSource(longs = { 123456789L, 0L, 256L, Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE - 1, Long.MIN_VALUE + 1,
			-1L })
	public void testLongToByteArrayAndBack(long originalValue) {
		byte[] byteArray = DataExportImportUtil.longToByteArray(originalValue);
		long reconstructedValue = DataExportImportUtil.byteArrayToLong(byteArray);
		assertEquals(originalValue, reconstructedValue, "Reconstructed value should match the original value");
	}

	@ParameterizedTest
	@MethodSource("provideInvalidByteArrays")
	public void testByteArrayToLongInvalidInput(byte[] invalidByteArray) {
		assertThrows(IllegalArgumentException.class, () -> {
			DataExportImportUtil.byteArrayToLong(invalidByteArray);
		}, "Should throw IllegalArgumentException for invalid byte array length");
	}

	private static Stream<byte[]> provideInvalidByteArrays() {
		return Stream.of(new byte[7], // Less than 8 bytes
				new byte[9] // More than 8 bytes
		);
	}
}
