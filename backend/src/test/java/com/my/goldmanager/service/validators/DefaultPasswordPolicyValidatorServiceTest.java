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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.my.goldmanager.service.exception.ValidationException;

public class DefaultPasswordPolicyValidatorServiceTest {
	public static class TestParameter {
		private String password;
		private boolean expectSuccess;

		public TestParameter(String password, boolean expectSuccess) {
			this.password = password;
			this.expectSuccess = expectSuccess;
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
			assertThrows(ValidationException.class,
					() -> new DefaultPasswordPolicyValidatorService().validate(testParameter.password),
					"Password must have a size between 8 and 100 characters and must contain of numbers, characters and at least one special character (e.g. \"@$!%*?&).");
		}
	}

	static Stream<TestParameter> generateTestParameter() {
		return Stream.of(new TestParameter(null, false), new TestParameter("", false), new TestParameter(" ", false), new TestParameter("a 1234567!", false),
				new TestParameter(
						"12345678901234567890123456789012345678901234567890123456789012345678!abcdEFGHIJKLMNOPQRST12345678901%",
						false),
				new TestParameter(
						"12345678901234567890123456789012345678901234567890123456789012345678!abcdEFGHIJKLMNOPQRST12345678901",
						true),new TestParameter("ABCde12!",true),new TestParameter("ABCde2!",false),new TestParameter("ABCde112",false),new TestParameter("ghghUgz67\"",true),new TestParameter("ghghUgz67§",true),new TestParameter("ghghUgz67€",true),new TestParameter("ghghUgz67{}",true),new TestParameter("ghghUgz67[]",true),new TestParameter("ghghUgz67äöü",true));
	}
}
