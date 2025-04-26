package com.my.goldmanager.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

import com.my.goldmanager.service.AuthenticationService;
import com.my.goldmanager.service.DataExportService;
import com.my.goldmanager.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
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

	@MockitoBean
	private DataExportService dataExportService;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void testExport_Success() throws Exception {
		byte[] mockData = "exportedData".getBytes();

		// Mock the service to return the expected data
		Mockito.when(dataExportService.exportData(Mockito.anyString())).thenReturn(mockData);

		mockMvc.perform(TestHTTPClient.doPost("/api/dataexport/export").contentType(MediaType.APPLICATION_JSON)
				.content("{\"password\": \"validPassword\"}")).andExpect(status().isOk())
				.andExpect(header().string("Content-Type", "application/octet-stream"))
				.andExpect(content().bytes(mockData));

		Mockito.verify(dataExportService, Mockito.times(1)).exportData(Mockito.anyString());
	}

	@Test
	void testExport_Error() throws Exception {
		// Mock the service to throw an exception
		Mockito.when(dataExportService.exportData(Mockito.anyString()))
				.thenThrow(new RuntimeException("Export failed"));

		mockMvc.perform(TestHTTPClient.doPost("/api/dataexport/export").contentType(MediaType.APPLICATION_JSON)
				.content("{\"password\": \"validPassword\"}")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Error exporting data: Export failed"));

		Mockito.verify(dataExportService, Mockito.times(1)).exportData(Mockito.anyString());
	}
}
