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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import jakarta.servlet.http.HttpServletResponse;

import com.my.goldmanager.rest.request.AuthRequest;
import com.my.goldmanager.service.AuthenticationService;
import com.my.goldmanager.service.entity.JWTTokenInfo;
import com.my.goldmanager.rest.response.AuthResponse;

@RestController()
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private AuthenticationService authenticationService;

       @PostMapping("/login")
       public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest, HttpServletResponse response) {
               try {
                       JWTTokenInfo tokenInfo = authenticationService.getJWTToken(authRequest.getUsername(), authRequest.getPassword());
                       ResponseCookie cookie = ResponseCookie.from("jwt-token", tokenInfo.getToken())
                                       .httpOnly(true)
                                       .path("/")
                                       .build();
                       response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
                       AuthResponse body = new AuthResponse(tokenInfo.getRefreshAfter(), tokenInfo.getEpiresOn());
                       return ResponseEntity.ok(body);
               } catch (AuthenticationException e) {
                       return ResponseEntity.status(401).build();
               }
       }

       @GetMapping("/csrf")
       public ResponseEntity<Void> csrf(jakarta.servlet.http.HttpServletRequest request) {
               org.springframework.security.web.csrf.CsrfToken token =
                               (org.springframework.security.web.csrf.CsrfToken) request
                                               .getAttribute(org.springframework.security.web.csrf.CsrfToken.class.getName());
               if (token != null) {
                       return ResponseEntity.noContent()
                                       .header("X-CSRF-TOKEN", token.getToken())
                                       .build();
               }
               return ResponseEntity.noContent().build();
       }

       @GetMapping("/refresh")
       public ResponseEntity<AuthResponse> refresh(HttpServletResponse response) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		try {
			if (authentication == null) { // No authentication information available
				// authentication.isAuthenticated() does not need to be checked, as it is
				// already checked in the filter
				// and the authentication object is null if not authenticated
				return ResponseEntity.status(401).build();
			}
                       JWTTokenInfo tokenInfo = authenticationService.refreshJWTToken(authentication.getName());
                       ResponseCookie cookie = ResponseCookie.from("jwt-token", tokenInfo.getToken())
                                       .httpOnly(true)
                                       .path("/")
                                       .build();
                       response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
                       AuthResponse body = new AuthResponse(tokenInfo.getRefreshAfter(), tokenInfo.getEpiresOn());
                       return ResponseEntity.ok(body);
		} catch (AuthenticationException e) {
			return ResponseEntity.status(401).build();
		}

	}

       @GetMapping("/logoutuser")
       public ResponseEntity<Void> logout(HttpServletResponse response) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		authenticationService.logout(authentication.getName());
               SecurityContextHolder.clearContext();
               ResponseCookie cookie = ResponseCookie.from("jwt-token", "")
                               .httpOnly(true)
                               .path("/")
                               .maxAge(0)
                               .build();
               response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
               return ResponseEntity.noContent().build();
	}

}
