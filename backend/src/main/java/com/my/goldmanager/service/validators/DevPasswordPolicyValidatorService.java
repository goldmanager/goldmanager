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
package com.my.goldmanager.service.validators;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.my.goldmanager.service.PasswordPolicyValidationService;
import com.my.goldmanager.service.exception.PasswordValidationException;

@Service
@Profile({"dev","test"})
public class DevPasswordPolicyValidatorService implements PasswordPolicyValidationService {

	@Override
	public void validate(String password) throws PasswordValidationException {
		if (password == null || password.isBlank() || password.trim().contains(" ")) {
			throw new PasswordValidationException("Password is mandatory and must not contain spaces.");
		}
		if (password.length() < 8) {
			throw new PasswordValidationException("Minimum password size is 8 characters.");
		}
		if (password.length() > 100) {
			throw new PasswordValidationException("Maximum password size is 100 characters.");
		}

	}

}
