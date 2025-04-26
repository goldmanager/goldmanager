package com.my.goldmanager.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.goldmanager.encoder.PasswordEncoderImpl;
import com.my.goldmanager.entity.UserLogin;
import com.my.goldmanager.repository.UserLoginRepository;
import com.my.goldmanager.rest.request.CreateUserRequest;
import com.my.goldmanager.rest.request.UpdateUserPasswordRequest;
import com.my.goldmanager.rest.request.UpdateUserStatusRequest;
import com.my.goldmanager.rest.response.ErrorResponse;
import com.my.goldmanager.rest.response.ListUserResponse;
import com.my.goldmanager.service.AuthenticationService;
import com.my.goldmanager.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserServiceControllerSpringBootTest {
	private final PasswordEncoderImpl passwordEncoder = new PasswordEncoderImpl();

	@Autowired
	private UserService userService;
	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private UserLoginRepository userLoginRepository;
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	public void setup() {
		TestHTTPClient.setup(userService, authenticationService);
	}

	@AfterEach
	public void cleanUp() {
		TestHTTPClient.cleanup();
		userLoginRepository.deleteAll();

	}

	@Test
	public void testCreateUser() throws Exception {
		CreateUserRequest createUserRequest = new CreateUserRequest();
		createUserRequest.setPassword("MyPass12345");
		createUserRequest.setUsername("myUser");

		mockMvc.perform(TestHTTPClient.doPost("/userService").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createUserRequest))).andExpect(status().isCreated());

		Optional<UserLogin> optional = userLoginRepository.findById("myUser");
		assertTrue(optional.isPresent());
		UserLogin result = optional.get();
		assertEquals(createUserRequest.getUsername(), result.getUserid());
		assertTrue(result.isActive());
		assertTrue(passwordEncoder.matches(createUserRequest.getPassword(),result.getPassword()));
	}

	@Test
	public void testDeleteUser() throws Exception {

		userService.create("MyUser", "MyPass12345");
		mockMvc.perform(TestHTTPClient.doDelete("/userService/deleteuser/MyUser")).andExpect(status().isNoContent());
	}

	@Test
	public void testDeleteCurrentUser() throws Exception {

		String body = mockMvc.perform(TestHTTPClient.doDelete("/userService/deleteuser/" + TestHTTPClient.username))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
		
		ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals(400, errorResponse.getStatus());
		assertEquals("Users must not delete themselves.", errorResponse.getMessage());

	}

	@Test
	public void testUpdateUserPassword() throws Exception {

		UserLogin userLogin = new UserLogin();
		userLogin.setActive(true);
		userLogin.setPassword("MyEncryptedPass");
		userLogin.setUserid("myUser");
		userLoginRepository.save(userLogin);

		UpdateUserPasswordRequest updateUserPasswordRequest = new UpdateUserPasswordRequest();
		updateUserPasswordRequest.setNewPassword("MyNewPass");

		mockMvc.perform(TestHTTPClient.doPut("/userService/updatePassword/" + userLogin.getUserid())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateUserPasswordRequest))).andExpect(status().isNoContent());

		Optional<UserLogin> optional = userLoginRepository.findById("myUser");
		assertTrue(optional.isPresent());
		UserLogin result = optional.get();
		assertEquals(userLogin.getUserid(), result.getUserid());
		assertTrue(result.isActive());
		assertTrue(passwordEncoder.matches(updateUserPasswordRequest.getNewPassword(),result.getPassword()));
	}

	@Test
	public void testUpdateUserStatus() throws Exception {
		UserLogin userLogin = new UserLogin();
		userLogin.setActive(false);
		userLogin.setPassword("MyEncryptedPass");
		userLogin.setUserid("myUser");
		userLoginRepository.save(userLogin);

		UpdateUserStatusRequest updateUserStatusRequest = new UpdateUserStatusRequest();
		updateUserStatusRequest.setActive(true);

		mockMvc.perform(TestHTTPClient.doPut("/userService/setStatus/" + userLogin.getUserid())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateUserStatusRequest))).andExpect(status().isNoContent());

		Optional<UserLogin> optional = userLoginRepository.findById("myUser");
		assertTrue(optional.isPresent());
		UserLogin result = optional.get();
		assertTrue(result.isActive());

		updateUserStatusRequest.setActive(false);
		mockMvc.perform(TestHTTPClient.doPut("/userService/setStatus/" + userLogin.getUserid())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateUserStatusRequest))).andExpect(status().isNoContent());

		optional = userLoginRepository.findById("myUser");
		assertTrue(optional.isPresent());
		result = optional.get();
		assertFalse(result.isActive());

		mockMvc.perform(
				TestHTTPClient.doPut("/userService/setStatus/invaliduser").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateUserStatusRequest)))
				.andExpect(status().isNotFound());

	}

	@Test
	public void testUpdateCurrentUserStatus() throws Exception {
		
		UpdateUserStatusRequest updateUserStatusRequest = new UpdateUserStatusRequest();
		updateUserStatusRequest.setActive(true);

		String body = mockMvc.perform(TestHTTPClient.doPut("/userService/setStatus/" + TestHTTPClient.username)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateUserStatusRequest))).andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();

		ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		
		assertEquals(400, errorResponse.getStatus());
		assertEquals("Users must not activate or deactivate themselves.", errorResponse.getMessage());

		updateUserStatusRequest.setActive(false);
		body = mockMvc.perform(TestHTTPClient.doPut("/userService/setStatus/" +  TestHTTPClient.username)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateUserStatusRequest))).andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();

		errorResponse = objectMapper.readValue(body, ErrorResponse.class);

		assertEquals(400, errorResponse.getStatus());
		assertEquals("Users must not activate or deactivate themselves.", errorResponse.getMessage());
	}
	@Test
	public void testUpdateUserPasswordInvalid() throws Exception {

		UserLogin userLogin = new UserLogin();
		userLogin.setActive(true);
		userLogin.setPassword("MyEncryptedPass");
		userLogin.setUserid("myUser");
		userLoginRepository.save(userLogin);

		UpdateUserPasswordRequest updateUserPasswordRequest = new UpdateUserPasswordRequest();
		updateUserPasswordRequest.setNewPassword(null);

		String body = mockMvc
				.perform(TestHTTPClient.doPut("/userService/updatePassword/" + userLogin.getUserid())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateUserPasswordRequest)))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals("Password is mandatory and must not contain spaces.", errorResponse.getMessage());
		assertEquals(400, errorResponse.getStatus());

		updateUserPasswordRequest = new UpdateUserPasswordRequest();
		updateUserPasswordRequest.setNewPassword("");

		body = mockMvc
				.perform(TestHTTPClient.doPut("/userService/updatePassword/" + userLogin.getUserid())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateUserPasswordRequest)))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals("Password is mandatory and must not contain spaces.", errorResponse.getMessage());
		assertEquals(400, errorResponse.getStatus());

	}

	@Test
	public void testUpdateUserPasswordUserNotExists() throws Exception {

		UpdateUserPasswordRequest updateUserPasswordRequest = new UpdateUserPasswordRequest();
		updateUserPasswordRequest.setNewPassword("MyNewPass");

		mockMvc.perform(
				TestHTTPClient.doPut("/userService/updatePassword/invaliduser").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateUserPasswordRequest)))
				.andExpect(status().isNotFound());
	}

	@Test
	public void testCreateUserExisting() throws Exception {

		UserLogin userLogin = new UserLogin();
		userLogin.setActive(true);
		userLogin.setPassword("EncodedPassword");
		userLogin.setUserid("myUser");
		userLoginRepository.save(userLogin);

		CreateUserRequest createUserRequest = new CreateUserRequest();
		createUserRequest.setPassword("MyPass12345");
		createUserRequest.setUsername("myUser");

		String body = mockMvc
				.perform(TestHTTPClient.doPost("/userService").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createUserRequest)))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals("Username 'myUser' already exists.", errorResponse.getMessage());
		assertEquals(400, errorResponse.getStatus());
	}

	@Test
	public void testCreateUserInvalidUser() throws Exception {

		CreateUserRequest createUserRequest = new CreateUserRequest();
		createUserRequest.setPassword("MyPass");
		createUserRequest.setUsername(null);

		String body = mockMvc
				.perform(TestHTTPClient.doPost("/userService").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createUserRequest)))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals("Username is mandatory, must not contain spaces and it's size must be between 1 and 255 alphanumeric characters.", errorResponse.getMessage());
		assertEquals(400, errorResponse.getStatus());

		createUserRequest = new CreateUserRequest();
		createUserRequest.setPassword("MyPass");
		createUserRequest.setUsername("");

		body = mockMvc
				.perform(TestHTTPClient.doPost("/userService").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createUserRequest)))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals("Username is mandatory, must not contain spaces and it's size must be between 1 and 255 alphanumeric characters.", errorResponse.getMessage());
		assertEquals(400, errorResponse.getStatus());
	}

	@Test
	public void testCreateUserInvalidPassword() throws Exception {

		CreateUserRequest createUserRequest = new CreateUserRequest();
		createUserRequest.setPassword(null);
		createUserRequest.setUsername("MyUser");

		String body = mockMvc
				.perform(TestHTTPClient.doPost("/userService").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createUserRequest)))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals("Password is mandatory and must not contain spaces.", errorResponse.getMessage());
		assertEquals(400, errorResponse.getStatus());

		createUserRequest = new CreateUserRequest();
		createUserRequest.setPassword("");
		createUserRequest.setUsername("MyUser");

		body = mockMvc
				.perform(TestHTTPClient.doPost("/userService").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createUserRequest)))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals("Password is mandatory and must not contain spaces.", errorResponse.getMessage());
		assertEquals(400, errorResponse.getStatus());
	}

	@Test
	public void testListAll() throws Exception {

		for (int current = 0; current < 10; current++) {
			UserLogin userLogin = new UserLogin();
			userLogin.setActive(current % 2 == 0);
			userLogin.setPassword("MyEncryptedPass" + current);
			userLogin.setUserid("myuser" + current);
			userLoginRepository.save(userLogin);
		}

		List<UserLogin> expectedUsers = userLoginRepository.findAll();
		String body = mockMvc.perform(TestHTTPClient.doGet("/userService")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse()
				.getContentAsString();
		ListUserResponse listUserResponse = objectMapper.readValue(body, ListUserResponse.class);
		assertEquals(expectedUsers.size(), listUserResponse.getUserInfos().size());

		for (int current = 0; current < expectedUsers.size(); current++) {

			assertEquals(expectedUsers.get(current).getUserid(),
					listUserResponse.getUserInfos().get(current).getUserName());
			assertEquals(expectedUsers.get(current).isActive(),
					listUserResponse.getUserInfos().get(current).isActive());
		}
	}
}
