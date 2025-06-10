package com.my.goldmanager.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;

import com.my.goldmanager.service.AuthenticationService;
import com.my.goldmanager.service.UserService;
import com.my.goldmanager.service.exception.ValidationException;

public class TestHTTPClient {

	private static final String contextPath = "/api";
	public static final String username = "testuser";
	private static final String pass = "testpass";

        private static String token = null;
        private static String csrfToken = null;
        private static UserService userService = null;
        private static AuthenticationService authenticationService = null;
        private static final CookieCsrfTokenRepository csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();

        public static void setup(UserService userService, AuthenticationService authenticationService) {
                TestHTTPClient.authenticationService = authenticationService;
                TestHTTPClient.userService = userService;
                try {
                        token = null;
                        csrfToken = null;
                        userService.create(username, pass);
                } catch (ValidationException e) {
                        // Nothing to Do
                }
        }

        public static void cleanup() {

                try {
                        token = null;
                        csrfToken = null;
                        userService.deleteUser(username, true);
                        userService = null;
                        authenticationService.logoutAll();
                        authenticationService = null;
                } catch (ValidationException e) {
			// Nothing to do
		}
	}

	public static MockHttpServletRequestBuilder doGet(String path) {
		return authenticate(get(setContextPath(path)));
	}

	public static MockHttpServletRequestBuilder doPost(String path) {
		return authenticate(post(setContextPath(path)));
	}

	public static MockHttpServletRequestBuilder doPut(String path) {
		return authenticate(put(setContextPath(path)));
	}

	public static MockHttpServletRequestBuilder doDelete(String path) {
		return authenticate(delete(setContextPath(path)));
	}

        public static MockHttpServletRequestBuilder authenticate(MockHttpServletRequestBuilder builder) {
                if (token == null) {
                        token = authenticationService.getJWTToken(username, pass).getToken();
                }
                if (csrfToken == null) {
                        MockHttpServletRequest request = new MockHttpServletRequest();
                        org.springframework.security.web.csrf.CsrfToken tokenObj = csrfRepo.generateToken(request);
                        csrfToken = tokenObj.getToken();
                }
                return builder.header("Authorization", "Bearer " + token)
                                .cookie(new Cookie("XSRF-TOKEN", csrfToken))
                                .header("X-XSRF-TOKEN", csrfToken);
        }


	private static String setContextPath(String path) {
		if (path.startsWith(contextPath + "/")) {
			return path;
		}
		if (!path.startsWith("/")) {
			return contextPath + "/" + path;
		}
		return contextPath + path;
	}
}
