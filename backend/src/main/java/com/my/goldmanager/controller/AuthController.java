/** Copyright 2024 fg12111

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.my.goldmanager.rest.request.AuthRequest;
import com.my.goldmanager.service.AuthenticationService;

@RestController()
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private AuthenticationService authenticationService;

	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody AuthRequest authRequest) {
		try {

			return ResponseEntity
					.ok(authenticationService.getJWTToken(authRequest.getUsername(), authRequest.getPassword()));
		} catch (AuthenticationException e) {
			return ResponseEntity.status(401).body("Unauthorized");
		}
	}

	@GetMapping("/refresh")
	public ResponseEntity<String> refresh() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return ResponseEntity.ok(authenticationService.refresJWTToken(authentication.getName()));
	}

	@GetMapping("/logoutuser")
	public ResponseEntity<Void> logout() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		authenticationService.logout(authentication.getName());
		SecurityContextHolder.clearContext();
		return ResponseEntity.noContent().build();
	}

}
