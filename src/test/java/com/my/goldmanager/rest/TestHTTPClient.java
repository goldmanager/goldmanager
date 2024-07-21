package com.my.goldmanager.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.my.goldmanager.service.AuthenticationService;
import com.my.goldmanager.service.UserService;
import com.my.goldmanager.service.exception.ValidationException;

public class TestHTTPClient {

	public static final String username = "testuser";
	private static final String pass = "testpass";

	private static String token = null;
	private static UserService userService = null;
	private static AuthenticationService authenticationService = null;

	public static void setup(UserService userService, AuthenticationService authenticationService) {
		TestHTTPClient.authenticationService = authenticationService;
		TestHTTPClient.userService = userService;
		try {
			token = null;
			userService.create(username, pass);
		} catch (ValidationException e) {
			// Nothing to Do
		}
	}

	public static void cleanup() {

		try {
			token = null;
			userService.deleteUser(username, true);
			userService = null;
			authenticationService.logoutAll();
			authenticationService = null;
		} catch (ValidationException e) {
			// Nothing to do
		}
	}

	public static MockHttpServletRequestBuilder doGet(String path) {
		return authenticate(get(path));
	}

	public static MockHttpServletRequestBuilder doPost(String path) {
		return authenticate(post(path));
	}

	public static MockHttpServletRequestBuilder doPut(String path) {
		return authenticate(put(path));
	}

	public static MockHttpServletRequestBuilder doDelete(String path) {
		return authenticate(delete(path));
	}

	public static MockHttpServletRequestBuilder authenticate(MockHttpServletRequestBuilder builder) {
		if (token == null) {
			token = authenticationService.getJWTToken(username, pass);
		}
		return builder.header("Authorization", "Bearer " + token);
	}

}