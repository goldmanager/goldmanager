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

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.my.goldmanager.entity.UserLogin;
import com.my.goldmanager.repository.UserLoginRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserLoginRepository userLoginRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Optional<UserLogin> userLoginOptional = userLoginRepository.findById(username);

		UserLogin userLogin = userLoginOptional.orElseThrow(() -> new UsernameNotFoundException("User not found."));
		UserBuilder builder = User.withUsername(userLogin.getUserid());
		builder.password(userLogin.getPassword());
		builder.disabled(!userLogin.isActive());
		builder.roles("USER");
		return builder.build();

	}

}
