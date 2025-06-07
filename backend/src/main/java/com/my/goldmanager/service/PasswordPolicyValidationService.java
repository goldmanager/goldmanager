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
package com.my.goldmanager.service;

import com.my.goldmanager.service.exception.ValidationException;

/**
 * Validates the given password according to its implemented password policy
 */
public interface PasswordPolicyValidationService {

	/**
	 * Validates that the given password is  valid according to implemented password validity and throws {@link ValidationException} containing the violated policy terms.
	 * @param password
	 * @throws ValidationException
	 */
	void validate(String password) throws ValidationException;
}
