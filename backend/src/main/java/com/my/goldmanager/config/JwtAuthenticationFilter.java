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
package com.my.goldmanager.config;

import java.io.IOException;
import java.security.Key;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.my.goldmanager.service.AuthKeyInfoService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Locator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Service
public class JwtAuthenticationFilter extends OncePerRequestFilter {

 
	@Autowired
	private AuthKeyInfoService authKeyInfoService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);
			try {
				Claims claims = Jwts.parser().keyLocator(new Locator<Key>() {

					@Override
					public Key locate(Header header) {

						return authKeyInfoService.getKeyforKeyId((String) header.get("keyId"));
					}
				}).build().parseSignedClaims(token).getPayload();
				String username = claims.getSubject();
				if (username != null) {
					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null,
							Collections.emptyList());
					SecurityContextHolder.getContext().setAuthentication(auth);
				}
			} catch (Exception e) {
				SecurityContextHolder.clearContext();
			}
		}
		filterChain.doFilter(request, response);
	}

}
