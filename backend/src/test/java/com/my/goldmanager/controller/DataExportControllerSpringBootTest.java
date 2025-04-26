package com.my.goldmanager.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.my.goldmanager.service.AuthenticationService;
import com.my.goldmanager.service.UserService;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class DataExportControllerSpringBootTest {
	@Autowired
	private UserService userService;
	@Autowired
	private AuthenticationService authenticationService;

	@BeforeEach
	public void setUp() {
		TestHTTPClient.setup(userService,authenticationService);
	}

	@AfterEach
	public void cleanUp() {
		TestHTTPClient.cleanup();
	}

	@Autowired
	private MockMvc mockMvc;

	@Test
	void testExport_ValidPassword() throws Exception {
		String requestBody = "{ \"password\": \"validPassword\" }";

		mockMvc.perform(TestHTTPClient.doPost("/api/dataexport/export").contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
	}

	@Test
	void testExport_EmptyPassword() throws Exception {
		String requestBody = "{ \"password\": \"\" }";

		mockMvc.perform(TestHTTPClient.doPost("/api/dataexport/export").contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Error exporting data: Encryption password is mandatory."));
	}

	@Test
	void testExport_MissingPassword() throws Exception {
		String requestBody = "{}";

		mockMvc.perform(TestHTTPClient.doPost("/api/dataexport/export").contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Error exporting data: Encryption password is mandatory."));
	}
}
