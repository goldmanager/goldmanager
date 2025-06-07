package com.my.goldmanager.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import com.my.goldmanager.rest.request.ExportDataRequest;
import com.my.goldmanager.service.AuthenticationService;
import com.my.goldmanager.service.DataExportService;
import com.my.goldmanager.service.DataExportStatusService;
import com.my.goldmanager.service.UserService;
import com.my.goldmanager.service.entity.JobStatus;

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
        TestHTTPClient.setup(userService, authenticationService);
    }

    @AfterEach
    public void cleanUp() {
        TestHTTPClient.cleanup();
    }

    @MockitoBean
    private DataExportService dataExportService;

    @Autowired
    private DataExportStatusService dataExportStatusService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testExport_AcceptedAndDownload() throws Exception {
        byte[] mockData = "exportedData".getBytes();
        Mockito.when(dataExportService.exportData(Mockito.anyString())).thenReturn(mockData);
        ExportDataRequest req = new ExportDataRequest();
        req.setPassword("validPassword");

        mockMvc.perform(TestHTTPClient.doPost("/api/dataexport/export").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isAccepted());

        Mockito.verify(dataExportService, Mockito.timeout(1000)).exportData(Mockito.anyString());
        Thread.sleep(50);
        org.junit.jupiter.api.Assertions.assertEquals(JobStatus.SUCCESS, dataExportStatusService.getStatus());

        mockMvc.perform(TestHTTPClient.doGet("/api/dataexport/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(content().bytes(mockData));
    }

    @Test
    void testExport_Conflict() throws Exception {
        Mockito.doAnswer(invocation -> {
            Thread.sleep(300);
            return "data".getBytes();
        }).when(dataExportService).exportData(Mockito.anyString());
        ExportDataRequest req = new ExportDataRequest();
        req.setPassword("validPassword");
        mockMvc.perform(TestHTTPClient.doPost("/api/dataexport/export").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isAccepted());
        mockMvc.perform(TestHTTPClient.doPost("/api/dataexport/export").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isConflict());
        Mockito.verify(dataExportService, Mockito.timeout(1000)).exportData(Mockito.anyString());
        Thread.sleep(350);
        org.junit.jupiter.api.Assertions.assertEquals(JobStatus.SUCCESS, dataExportStatusService.getStatus());
    }
}
