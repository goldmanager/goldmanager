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

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.my.goldmanager.service.entity.JWTTokenInfo;
import com.my.goldmanager.service.entity.KeyInfo;

import io.jsonwebtoken.Jwts;

@Service
public class AuthenticationService {

       @Value("${com.my.goldmanager.auth.jwtTokenValidity:7200000}") // Default: 2h
       private long jwtTokenValidity;
       @Value("${com.my.goldmanager.auth.jwtTokenRefreshStart:3600000}") // Default: 1h
       private long jwtTokenRefreshStart;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private AuthKeyInfoService authKeyInfoService;

	public JWTTokenInfo getJWTToken(String username, String password) {
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(username, password));

		User user = (User) authentication.getPrincipal();
		KeyInfo keyInfo = authKeyInfoService.getKeyInfoForUserName(user.getUsername());

		return buildJWTToken(user.getUsername(), keyInfo);

	}

	private JWTTokenInfo buildJWTToken(String username, KeyInfo keyInfo) {
		Date issuedDate = new Date(System.currentTimeMillis());
		Date expirationDate = new Date(issuedDate.toInstant().toEpochMilli() + jwtTokenValidity);
		Date refreshDate = new Date(issuedDate.toInstant().toEpochMilli() + jwtTokenRefreshStart);
		JWTTokenInfo result = new JWTTokenInfo();
		result.setEpiresOn(expirationDate);
		result.setRefreshAfter(refreshDate);

		result.setToken(Jwts.builder().header().add("keyId", keyInfo.getKeyId()).and().subject(username)
				.issuedAt(issuedDate).expiration(expirationDate).signWith(keyInfo.getKey()).compact());
		return result;
	}


  public JWTTokenInfo refreshJWTToken(String username) {
               KeyInfo keyInfo = authKeyInfoService.getKeyInfoForUserName(username);
               return buildJWTToken(username, keyInfo);

	}

	public void logout(String username) {
		authKeyInfoService.removeKeyInfosForUserName(username);

	}

	public void logoutAll() {
		authKeyInfoService.clearAll();
	}

}
