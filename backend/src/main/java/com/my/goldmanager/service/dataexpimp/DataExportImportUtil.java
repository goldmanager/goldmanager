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

/**
 * Utility class for data export and import operations.
 */
public class DataExportImportUtil {

	private DataExportImportUtil() {
		// Prevent instantiation
	}

	/**
	 * Converts a long value to a byte array.
	 *
	 * @param long value the long value to convert
	 * @return the byte array representation of the long value
	 */
	public static byte[] longToByteArray(long value) {
		byte[] byteArray = new byte[8];
		for (int i = 0; i < 8; i++) {
			byteArray[7 - i] = (byte) (value >> (i * 8));
		}
		return byteArray;
	}

	/**
	 * Converts a byte array to a long value.
	 *
	 * @param byteArray the byte array to convert
	 * @return the long value representation of the byte array
	 */
	public static long byteArrayToLong(byte[] byteArray) {
		if (byteArray.length != 8) {
			throw new IllegalArgumentException("Byte array must be 8 bytes long");
		}
		long value = 0;
		for (int i = 0; i < 8; i++) {
			value = (value << 8) | (byteArray[i] & 0xFF);
		}
		return value;
	}
}
