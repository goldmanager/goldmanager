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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.my.goldmanager.service.exception.PasswordValidationException;

class DevPasswordPolicyValidatorServiceTest {

    private final DevPasswordPolicyValidatorService validator = new DevPasswordPolicyValidatorService();

    @Test
    @DisplayName("rejects null, blank and containing spaces")
    void rejectsInvalidBasics() {
        assertThrows(PasswordValidationException.class, () -> validator.validate(null));
        assertThrows(PasswordValidationException.class, () -> validator.validate(""));
        assertThrows(PasswordValidationException.class, () -> validator.validate("   "));
        assertThrows(PasswordValidationException.class, () -> validator.validate("abc def"));
    }

    @Test
    @DisplayName("rejects too short and too long passwords")
    void rejectsLengthBounds() {
        assertThrows(PasswordValidationException.class, () -> validator.validate("short7")); // 6+1 = 7 chars

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 101; i++) sb.append('a');
        assertThrows(PasswordValidationException.class, () -> validator.validate(sb.toString()));
    }

    @Test
    @DisplayName("accepts valid password within dev policy")
    void acceptsValidDevPassword() {
        // 8+ chars, no spaces, within max length
        assertDoesNotThrow(() -> validator.validate("abcdefgh"));
        assertDoesNotThrow(() -> validator.validate("abc12345"));
        assertDoesNotThrow(() -> validator.validate("valid_password_123"));
    }
}

