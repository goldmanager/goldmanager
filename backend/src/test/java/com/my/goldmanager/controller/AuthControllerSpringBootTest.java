package com.my.goldmanager.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import jakarta.servlet.http.Cookie;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.goldmanager.repository.UserLoginRepository;
import com.my.goldmanager.rest.request.AuthRequest;
import com.my.goldmanager.service.AuthKeyInfoService;
import com.my.goldmanager.service.AuthenticationService;
import com.my.goldmanager.service.UserService;
import com.my.goldmanager.rest.response.AuthResponse;
import com.my.goldmanager.service.entity.KeyInfo;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerSpringBootTest {

	@Autowired
	private AuthKeyInfoService authKeyInfoService;

	@Autowired
	private UserLoginRepository userLoginRepository;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private UserService userService;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@AfterEach
	public void cleanUp() {
		userLoginRepository.deleteAll();
		authenticationService.logoutAll();
	}

	@Test
        public void testLoginSuccess() throws JsonProcessingException, Exception {
		userService.create("user", "password");
		AuthRequest authRequest = new AuthRequest();
		authRequest.setUsername("user");
		authRequest.setPassword("password");

                MvcResult result = mockMvc
                                .perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(authRequest)))
                                .andExpect(status().isOk()).andReturn();

                String cookie = result.getResponse().getCookie("jwt-token").getValue();
                AuthResponse jwtTokenInfo = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
                assertNotNull(jwtTokenInfo);
                assertNotNull(jwtTokenInfo.getEpiresOn());
                assertNotNull(jwtTokenInfo.getRefreshAfter());
                assertTrue(jwtTokenInfo.getEpiresOn().after(jwtTokenInfo.getRefreshAfter()));

                mockMvc.perform(get("/api/userService").cookie(new Cookie("jwt-token", cookie)))
                                .andExpect(status().isOk());

        }

        @Test
        public void testCsrfEndpoint() throws Exception {
                mockMvc.perform(get("/api/auth/csrf"))
                                .andExpect(status().isNoContent());
        }

	@Test
	public void testLoginFailure() throws JsonProcessingException, Exception {
		userService.create("user", "password");
		AuthRequest authRequest = new AuthRequest();
		authRequest.setUsername("user");
		authRequest.setPassword("password1");

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(authRequest))).andExpect(status().isUnauthorized());

	}

	@Test
	public void testLogout() throws JsonProcessingException, Exception {
		userService.create("user", "password");
		AuthRequest authRequest = new AuthRequest();
		authRequest.setUsername("user");
		authRequest.setPassword("password");

                MvcResult result = mockMvc
                                .perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(authRequest)))
                                .andExpect(status().isOk()).andReturn();

                String cookie = result.getResponse().getCookie("jwt-token").getValue();

                mockMvc.perform(get("/api/auth/logoutuser").cookie(new Cookie("jwt-token", cookie)))
                                .andExpect(status().isNoContent());

                mockMvc.perform(get("/api/userService").cookie(new Cookie("jwt-token", cookie)))
                                .andExpect(status().is(403));
	}

	@Test
	public void testRefresh() throws JsonProcessingException, Exception {
		userService.create("user", "password");
		AuthRequest authRequest = new AuthRequest();
		authRequest.setUsername("user");
		authRequest.setPassword("password");

                MvcResult result = mockMvc
                                .perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(authRequest)))
                                .andExpect(status().isOk()).andReturn();
                String cookie = result.getResponse().getCookie("jwt-token").getValue();
                KeyInfo oldKey = authKeyInfoService.getKeyInfoForUserName("user");
                AuthResponse jwtTokenInfo = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
                assertNotNull(jwtTokenInfo);
                assertNotNull(jwtTokenInfo.getEpiresOn());
                assertNotNull(jwtTokenInfo.getRefreshAfter());
                assertTrue(jwtTokenInfo.getEpiresOn().after(jwtTokenInfo.getRefreshAfter()));

                MvcResult refreshResult = mockMvc
                                .perform(get("/api/auth/refresh").cookie(new Cookie("jwt-token", cookie)))
                                .andExpect(status().isOk()).andReturn();
                String newTokenValue = refreshResult.getResponse().getCookie("jwt-token").getValue();
                KeyInfo newKey = authKeyInfoService.getKeyInfoForUserName("user");

                AuthResponse newJWTTokenInfo = objectMapper.readValue(refreshResult.getResponse().getContentAsString(), AuthResponse.class);
                assertNotNull(newJWTTokenInfo);
                assertNotNull(newJWTTokenInfo.getEpiresOn());
                assertNotNull(newJWTTokenInfo.getRefreshAfter());
                assertTrue(newJWTTokenInfo.getEpiresOn().after(newJWTTokenInfo.getRefreshAfter()));
                assertTrue(newJWTTokenInfo.getEpiresOn().after(jwtTokenInfo.getEpiresOn()));
                assertTrue(newJWTTokenInfo.getRefreshAfter().after(jwtTokenInfo.getRefreshAfter()));
                assertEquals(oldKey.getKeyId(), newKey.getKeyId());

                assertArrayEquals(oldKey.getKey().getEncoded(), newKey.getKey().getEncoded());

                mockMvc.perform(get("/api/userService").cookie(new Cookie("jwt-token", newTokenValue)))
                                .andExpect(status().isOk());
	}

	@Test
	public void testRefreshWithInvalidatedKey() throws JsonProcessingException, Exception {
		userService.create("user", "password");
		AuthRequest authRequest = new AuthRequest();
		authRequest.setUsername("user");
		authRequest.setPassword("password");

                MvcResult result = mockMvc
                                .perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(authRequest)))
                                .andExpect(status().isOk()).andReturn();
                String cookie = result.getResponse().getCookie("jwt-token").getValue();
                KeyInfo oldKey = authKeyInfoService.getKeyInfoForUserName("user");

                authKeyInfoService.invalidateKeyForUsername("user");

                AuthResponse jwtTokenInfo = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
                assertNotNull(jwtTokenInfo);
                assertNotNull(jwtTokenInfo.getEpiresOn());
                assertNotNull(jwtTokenInfo.getRefreshAfter());
                
                MvcResult refreshResult = mockMvc
                                .perform(get("/api/auth/refresh").cookie(new Cookie("jwt-token", cookie)))
                                .andExpect(status().isOk()).andReturn();

                AuthResponse newJWTTokenInfo = objectMapper.readValue(refreshResult.getResponse().getContentAsString(), AuthResponse.class);
                assertNotNull(newJWTTokenInfo);
                assertNotNull(newJWTTokenInfo.getEpiresOn());
                assertNotNull(newJWTTokenInfo.getRefreshAfter());
                KeyInfo newKey = authKeyInfoService.getKeyInfoForUserName("user");

                assertNotEquals(oldKey.getKeyId(), newKey.getKeyId());
                assertFalse(Arrays.equals(oldKey.getKey().getEncoded(), newKey.getKey().getEncoded()));

                mockMvc.perform(get("/api/userService").cookie(new Cookie("jwt-token", refreshResult.getResponse().getCookie("jwt-token").getValue())))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/userService").cookie(new Cookie("jwt-token", cookie)))
                                .andExpect(status().isOk());

	}

	@Test
	public void testRefreshWithRemovedUser() throws JsonProcessingException, Exception {
		userService.create("user", "password");
		AuthRequest authRequest = new AuthRequest();
		authRequest.setUsername("user");
		authRequest.setPassword("password");

                MvcResult result = mockMvc
                                .perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(authRequest)))
                                .andExpect(status().isOk()).andReturn();

                userService.deleteUser("user", true);

                String cookie = result.getResponse().getCookie("jwt-token").getValue();

                AuthResponse jwtTokenInfo = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
                assertNotNull(jwtTokenInfo);
                assertNotNull(jwtTokenInfo.getEpiresOn());
                assertNotNull(jwtTokenInfo.getRefreshAfter());
                mockMvc
                                .perform(get("/api/auth/refresh").cookie(new Cookie("jwt-token", cookie)))
                                .andExpect(status().is(403));

	}
}
