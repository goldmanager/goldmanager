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
package com.my.goldmanager.service.exception;

/**
 * Exception for Loading Problems of Versions
 */
public class VersionLoadingException extends Exception {

	private static final long serialVersionUID = -3532962077848294307L;

	public VersionLoadingException(String message, Throwable cause) {
		super(message, cause);

	}

	public VersionLoadingException(String message) {
		super(message);
	}

}
