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
package com.my.goldmanager.encoder;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderImpl implements PasswordEncoder {
	private final Argon2PasswordEncoder innerEncoder= new Argon2PasswordEncoder(16, 32, 1, 1 << 12, 3);
	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		return innerEncoder.matches(rawPassword, encodedPassword);
		
	}

	@Override
	public String encode(CharSequence rawPassword) {
		return innerEncoder.encode(rawPassword);
	}

}
