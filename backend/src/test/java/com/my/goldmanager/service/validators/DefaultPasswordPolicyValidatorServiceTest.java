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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.my.goldmanager.service.exception.PasswordValidationException;

public class DefaultPasswordPolicyValidatorServiceTest {
	public static class TestParameter {
		private String password;
		private boolean expectSuccess;
		private String expectedMessage;

		public TestParameter(String password, boolean expectSuccess, String expectedMessage) {
			this.password = password;
			this.expectSuccess = expectSuccess;
			this.expectedMessage = expectedMessage;

		}

		@Override
		public String toString() {
			return "TestParameter [password=" + password + ", expectSuccess=" + expectSuccess + "]";
		}

	}

	@ParameterizedTest
	@MethodSource("generateTestParameter")
	void testValidate(TestParameter testParameter) {
		if (testParameter.expectSuccess) {
			assertDoesNotThrow(() -> new DefaultPasswordPolicyValidatorService().validate(testParameter.password));
		} else {
			PasswordValidationException ex = assertThrows(PasswordValidationException.class,
					() -> new DefaultPasswordPolicyValidatorService().validate(testParameter.password));
			Assertions.assertEquals(testParameter.expectedMessage, ex.getMessage());
		}
	}

	static Stream<TestParameter> generateTestParameter() {
		return Stream.of(new TestParameter(null, false, "Password cannot be null or blank."),
				new TestParameter("", false, "Password cannot be null or blank."),
				new TestParameter(" ", false, "Password cannot be null or blank."),
				new TestParameter("a 1234567!", false,
						"Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character."),
				new TestParameter("aaa", false,
						"Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character."),
				new TestParameter("aaaaaaaa", false,
						"Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character."),
				new TestParameter(generateLongassword(101, "a"), false,
						"Password cannot exceed 100 characters."),
				new TestParameter(
						"12345678901234567890123456789012345678901234567890123456789012345678!abcdEFGHIJKLMNOPQRST12345678901%",
						false, "Password cannot exceed 100 characters."),
				new TestParameter(
						"12345678901234567890123456789012345678901234567890123456789012345678!abcdEFGHIJKLMNOPQRST12345678901",
						true, null),
				new TestParameter("ABCde12!", true, null),
				new TestParameter("ABCde2!", false,
						"Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character."),
				new TestParameter("ABCde112", false,
						"Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character."),
				new TestParameter("ghghUgz67\"", true, null),
				new TestParameter("ghghUgz67§", true, null), new TestParameter("ghghUgz67€", true, null),
				new TestParameter("ghghUgz67{}", true, null), new TestParameter("ghghUgz67[]", true, null),
				new TestParameter("ghghUgz67äöü", true, null));
	}

	private static String generateLongassword(int length, String base) {

		StringBuilder password = new StringBuilder(length);

		while (password.length() < length) {
			password.append(base);
		}
		return password.toString();
	}
}
