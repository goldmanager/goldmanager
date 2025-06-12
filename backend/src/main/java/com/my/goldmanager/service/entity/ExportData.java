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
package com.my.goldmanager.service.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * Representation of exported data
 */
public class ExportData {

	@Getter
	@Setter
	/**
	 * Application Version of exported data
	 */
	private String version;

	@Getter
	@Setter
	/**
	 * The serialized {@link ExportEntities}
	 */
	private byte[] exportEntityData;

	@Getter
	@Setter
	/**
	 * the algorithm used for hashing the data 
	 */
	private String hashAlgorithm;

	@Getter
	@Setter
	/**
	 * The hash of the serialized {@link ExportEntities}
	 */
	private byte[] hash;
}
