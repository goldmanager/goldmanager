package com.my.goldmanager.controller;

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
import com.my.goldmanager.service.ImportStatusService;
import com.my.goldmanager.service.UserService;
import com.my.goldmanager.service.entity.JobStatus;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DataImportControllerSpringBootTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DataImportService dataImportService;

    @Autowired
    private ImportStatusService importStatusService;

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
    void testImportData_Accepted() throws Exception {
        ImportDataRequest request = new ImportDataRequest();
        request.setData("validData".getBytes());
        request.setPassword("validPassword");

        String response = mockMvc
                .perform(TestHTTPClient.doPost("/api/dataimport/import").contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();

        org.junit.jupiter.api.Assertions.assertTrue(response.isEmpty());
        Mockito.verify(dataImportService, Mockito.timeout(1000).times(1)).importData(request.getData(),
                request.getPassword());
        Mockito.verify(dataImportService, Mockito.timeout(1000)).importData(Mockito.any(), Mockito.any());
        Thread.sleep(50);
        org.junit.jupiter.api.Assertions.assertEquals(JobStatus.SUCCESS, importStatusService.getStatus());
    }

    @Test
    void testImportData_Conflict() throws Exception {
        Mockito.doAnswer(invocation -> {
            Thread.sleep(300);
            return null;
        }).when(dataImportService).importData(Mockito.any(), Mockito.any());

        ImportDataRequest request = new ImportDataRequest();
        request.setData("validData".getBytes());
        request.setPassword("validPassword");
        String response = mockMvc
                .perform(TestHTTPClient.doPost("/api/dataimport/import").contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
        // second call while running
        mockMvc.perform(TestHTTPClient.doPost("/api/dataimport/import").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request))).andExpect(status().isConflict());

        Mockito.verify(dataImportService, Mockito.timeout(1000)).importData(Mockito.any(), Mockito.any());
        Thread.sleep(350);
        org.junit.jupiter.api.Assertions.assertEquals(JobStatus.SUCCESS, importStatusService.getStatus());
    }

    @Test
    void testStatusEndpoint() throws Exception {
        ImportDataRequest request = new ImportDataRequest();
        request.setData("validData".getBytes());
        request.setPassword("validPassword");
        String response = mockMvc
                .perform(TestHTTPClient.doPost("/api/dataimport/import").contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
        mockMvc.perform(TestHTTPClient.doGet("/api/dataimport/status"))
                .andExpect(status().isOk());
        Mockito.verify(dataImportService, Mockito.timeout(1000)).importData(Mockito.any(), Mockito.any());
        Thread.sleep(50);
        mockMvc.perform(TestHTTPClient.doGet("/api/dataimport/status"))
                .andExpect(status().isOk());
    }

    @Test
    void testImportData_WrongPasswordResultsInFailedStatus() throws Exception {
        Mockito.doThrow(new IllegalArgumentException(
                "Reading of decrypted data failed, maybe the provided password is incorrect?"))
                .when(dataImportService).importData(Mockito.any(), Mockito.any());

        ImportDataRequest request = new ImportDataRequest();
        request.setData("invalidData".getBytes());
        request.setPassword("wrongPassword");

        String response = mockMvc
                .perform(TestHTTPClient.doPost("/api/dataimport/import").contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();

        Mockito.verify(dataImportService, Mockito.timeout(1000)).importData(Mockito.any(), Mockito.any());
        Thread.sleep(50);
        org.junit.jupiter.api.Assertions.assertEquals(JobStatus.FAILED, importStatusService.getStatus());
    }
}
