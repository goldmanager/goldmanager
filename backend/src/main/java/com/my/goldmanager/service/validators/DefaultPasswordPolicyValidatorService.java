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

import java.util.regex.Pattern;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.my.goldmanager.service.PasswordPolicyValidationService;
import com.my.goldmanager.service.exception.PasswordValidationException;

@Service
@Profile("default")
public class DefaultPasswordPolicyValidatorService implements PasswordPolicyValidationService {

	/**
	 * Password policy: - At least 8 characters long - At least one uppercase letter
	 * - At least one lowercase letter - At least one digit - At least one special
	 * character (e.g., @, $, !, %, *, ?, &, ä, ö, ü, Ä, Ö, Ü, ß, €, §, ", }, {, ],
	 * [)
	 */
	private final Pattern pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&äöüÄÖÜß€§\"\\}\\{\\]\\[]).{8,}$");

	@Override
	public void validate(String password) throws PasswordValidationException {
		if (password == null || password.isBlank()) {
			throw new PasswordValidationException("Password cannot be null or blank.");
		}
		String toValidate = password.trim();
		if (toValidate.length() > 100) {
			throw new PasswordValidationException("Password cannot exceed 100 characters.");
		}
		if (!pattern.matcher(toValidate).matches()) {
			throw new PasswordValidationException(
					"Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character.");
		}


	}

}
