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
package com.my.goldmanager.controller;

import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.my.goldmanager.rest.entity.UserInfo;
import com.my.goldmanager.rest.request.CreateUserRequest;
import com.my.goldmanager.rest.request.UpdateUserPasswordRequest;
import com.my.goldmanager.rest.request.UpdateUserStatusRequest;
import com.my.goldmanager.rest.response.ErrorResponse;
import com.my.goldmanager.rest.response.ListUserResponse;
import com.my.goldmanager.service.UserService;
import com.my.goldmanager.service.exception.BadRequestException;
import com.my.goldmanager.service.exception.ValidationException;

@RestController
@RequestMapping("/api/userService")
public class UserServiceController {

	@Autowired
	private UserService userService;

	@PostMapping
	public ResponseEntity<Void> createUser(@RequestBody CreateUserRequest createUserRequest) {

		try {
			userService.create(createUserRequest.getUsername(), createUserRequest.getPassword());
			return ResponseEntity.status(201).build();
		} catch (ValidationException e) {
			throw new BadRequestException(e.getMessage());
		}

	}

	@DeleteMapping(path = "/deleteuser/{userId}")
	public ResponseEntity<Void> DeleteUserName(@PathVariable("userId") String userId) {

		try {
			if (userService.deleteUser(userId)) {
				return ResponseEntity.noContent().build();
			}
		} catch (ValidationException e) {
			throw new BadRequestException(e.getMessage());
		}
		return ResponseEntity.notFound().build();
	}

	@PutMapping(path = "/updatePassword/{userId}")
	public ResponseEntity<Void> updateUserPassword(@PathVariable("userId") String userId,
			@RequestBody UpdateUserPasswordRequest updateUserPasswordRequest) {

		try {
			if (userService.updatePassword(userId, updateUserPasswordRequest.getNewPassword())) {
				return ResponseEntity.noContent().build();
			}
		} catch (ValidationException e) {
			throw new BadRequestException(e.getMessage());
		}
		return ResponseEntity.notFound().build();
	}

	@PutMapping(path = "/setStatus/{userId}")
	public ResponseEntity<Void> updateUserUserStatus(@PathVariable("userId") String userId,
			@RequestBody UpdateUserStatusRequest updateUserStatusRequest) {

		try {
			if (userService.updateUserActivation(userId, updateUserStatusRequest.isActive())) {
				return ResponseEntity.noContent().build();
			}
		} catch (ValidationException e) {
			throw new BadRequestException(e.getMessage());
		}

		return ResponseEntity.notFound().build();
	}

	@GetMapping
	public ResponseEntity<ListUserResponse> listAll() {
		ListUserResponse result = new ListUserResponse();
		result.setUserInfos(new LinkedList<>());
		userService.listAll().stream().forEach(u -> {
			UserInfo userInfo = new UserInfo();
			userInfo.setUserName(u.getUserid());
			userInfo.setActive(u.isActive());
			result.getUserInfos().add(userInfo);
		});
		return ResponseEntity.ok(result);
	}

	@ExceptionHandler(BadRequestException.class)
	public final ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, WebRequest request) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}
}
