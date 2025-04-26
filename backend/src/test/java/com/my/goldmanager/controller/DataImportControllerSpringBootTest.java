package com.my.goldmanager.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.goldmanager.rest.request.ImportDataRequest;
import com.my.goldmanager.service.AuthenticationService;
import com.my.goldmanager.service.DataImportService;
import com.my.goldmanager.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DataImportControllerSpringBootTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private DataImportService dataImportService;
	@Autowired
	private UserService userService;
	@Autowired
	private AuthenticationService authenticationService;

	@BeforeEach
	public void setUp() {
		TestHTTPClient.setup(userService, authenticationService);
	}

	@AfterEach
	public void cleanUp() {
		TestHTTPClient.cleanup();
	}

	@Test
	void testImportData_Success() throws Exception {
		ImportDataRequest request = new ImportDataRequest();
		request.setData("validData".getBytes());
		request.setPassword("validPassword");

		mockMvc.perform(TestHTTPClient.doPost("/api/dataimport/import").contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request))).andExpect(status().isNoContent());

		Mockito.verify(dataImportService, Mockito.times(1)).importData(Mockito.eq(request.getData()),
				Mockito.eq(request.getPassword()));
	}

	@Test
	void testImportData_EmptyPassword() throws Exception {
		ImportDataRequest request = new ImportDataRequest();
		request.setData("validData".getBytes());
		request.setPassword("");

		Mockito.doThrow(new IllegalArgumentException("Encryption password cannot be null or empty"))
				.when(dataImportService).importData(request.getData(), request.getPassword());

		mockMvc.perform(TestHTTPClient.doPost("/api/dataimport/import").contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Error importing data: Encryption password cannot be null or empty"));
	}

	@Test
	void testImportData_NullPassword() throws Exception {
		ImportDataRequest request = new ImportDataRequest();
		request.setData("validData".getBytes());
		request.setPassword(null);
		Mockito.doThrow(new IllegalArgumentException("Encryption password cannot be null or empty"))
				.when(dataImportService).importData(request.getData(), request.getPassword());

		mockMvc.perform(TestHTTPClient.doPost("/api/dataimport/import").contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Error importing data: Encryption password cannot be null or empty"));
	}

	@Test
	void testImportData_EmptyData() throws Exception {
		ImportDataRequest request = new ImportDataRequest();
		request.setData(new byte[0]);
		request.setPassword("validPassword");
		Mockito.doThrow(new IllegalArgumentException("Data cannot be null or empty")).when(dataImportService)
				.importData(request.getData(), request.getPassword());
		mockMvc.perform(TestHTTPClient.doPost("/api/dataimport/import").contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Error importing data: Data cannot be null or empty"));
	}

	@Test
	void testImportData_NullData() throws Exception {
		ImportDataRequest request = new ImportDataRequest();
		request.setData(null);
		request.setPassword("validPassword");
		Mockito.doThrow(new IllegalArgumentException("Data cannot be null or empty")).when(dataImportService)
				.importData(request.getData(), request.getPassword());
		mockMvc.perform(TestHTTPClient.doPost("/api/dataimport/import").contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Error importing data: Data cannot be null or empty"));
	}
}
