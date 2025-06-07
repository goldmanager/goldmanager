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

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.my.goldmanager.encoder.PasswordEncoderImpl;
import com.my.goldmanager.entity.UserLogin;
import com.my.goldmanager.repository.UserLoginRepository;
import com.my.goldmanager.service.exception.ValidationException;

@Service
public class UserService {

	private final PasswordEncoderImpl passwordEncoder = new PasswordEncoderImpl();

	@Autowired
	private PasswordPolicyValidationService passwordPolicyValidationService;
	@Autowired
	private AuthenticationService authenticationService;
	@Autowired
	private UserLoginRepository userLoginRepository;

	/**
	 * Create a new user
	 * 
	 * @param username
	 * @param password
	 * @throws ValidationException
	 */
	@Transactional
	public void create(String username, String password) throws ValidationException {

		if (username == null || username.isBlank() || username.trim().contains(" ") || username.length()>255) {
			throw new ValidationException("Username is mandatory, must not contain spaces and it's size must be between 1 and 255 alphanumeric characters.");
		}
		passwordPolicyValidationService.validate(password);
		if (userLoginRepository.existsById(username.trim())) {
			throw new ValidationException("Username '" + username.trim() + "' already exists.");
		}
		UserLogin userLogin = new UserLogin();
		userLogin.setActive(true);
		userLogin.setPassword(passwordEncoder.encode(password.trim()));
		userLogin.setUserid(username.trim());
		userLoginRepository.save(userLogin);

	}

	/**
	 * Deletes the specified user.
	 * 
	 * @param username
	 * @return
	 * @throws ValidationException
	 */
	public boolean deleteUser(String username) throws ValidationException {
		return deleteUserInternal(username, false);
	}

	/**
	 * Deletes the specified user.
	 * 
	 * @param username
	 * @param force
	 * @return
	 * @throws ValidationException
	 */
	public boolean deleteUser(String username, boolean force) throws ValidationException {
		return deleteUserInternal(username, force);
	}
	/**
	 * 
	 * @param username
	 * @param force
	 * @return
	 * @throws ValidationException
	 */
	@Transactional
	private boolean deleteUserInternal(String username, boolean force) throws ValidationException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!force && authentication != null && username.equals(authentication.getName())) {
			throw new ValidationException("Users must not delete themselves.");
		}
		if (userLoginRepository.existsById(username)) {
			userLoginRepository.deleteById(username);
			authenticationService.logout(username);
			return true;
		}
		return false;
	}

	/**
	 * Update the user's password
	 * 
	 * @param username
	 * @param newPassword
	 * @return
	 * @throws ValidationException
	 */
	@Transactional
	public boolean updatePassword(String username, String newPassword) throws ValidationException {

		if (username == null || username.isBlank()) {
			throw new ValidationException("Username is mandatory");
		}
		passwordPolicyValidationService.validate(newPassword);
		Optional<UserLogin> userlogin = userLoginRepository.findById(username);
		if (userlogin.isPresent()) {
			UserLogin login = userlogin.get();
			login.setPassword(passwordEncoder.encode(newPassword));
			userLoginRepository.save(login);
			return true;
		}
		return false;
	}

	/**
	 * Update user status
	 * 
	 * @param username
	 * @param active
	 * @return
	 * @throws ValidationException
	 */
	@Transactional
	public boolean updateUserActivation(String username, boolean active) throws ValidationException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && username.equals(authentication.getName())) {
			throw new ValidationException("Users must not activate or deactivate themselves.");
		}

		Optional<UserLogin> userlogin = userLoginRepository.findById(username);

		if (userlogin.isPresent()) {

			UserLogin login = userlogin.get();
			login.setActive(active);
			userLoginRepository.save(login);
			if (!active) {
				authenticationService.logout(username);
			}
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @return returns the number of all existing users
	 */
	public long countUsers() {
		return userLoginRepository.count();
	}

	/**
	 * 
	 * @return all existing users
	 */
	public List<UserLogin> listAll() {
		return userLoginRepository.findAll();
	}
}
