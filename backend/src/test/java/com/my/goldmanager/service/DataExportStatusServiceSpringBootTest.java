package com.my.goldmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.my.goldmanager.service.entity.JobStatus;
import com.my.goldmanager.service.exception.ExportInProgressException;

@SpringBootTest
@ActiveProfiles("test")
class DataExportStatusServiceSpringBootTest {

    @Autowired
    private DataExportStatusService dataExportStatusService;

    @MockitoBean
    private DataExportService dataExportService;

    @Test
    void testStartExportInvokesAsync() throws Exception {
        Mockito.when(dataExportService.exportData(Mockito.any())).thenReturn("data".getBytes());
        dataExportStatusService.startExport("pass");
        Mockito.verify(dataExportService, Mockito.timeout(1000)).exportData(Mockito.any());
        Thread.sleep(50);
        assertEquals(JobStatus.SUCCESS, dataExportStatusService.getStatus());
        assertEquals("", dataExportStatusService.getMessage());
    }

    @Test
    void testStartExportWhileRunningThrows() throws Exception {
        Mockito.doAnswer(i -> {
            Thread.sleep(300);
            return "data".getBytes();
        }).when(dataExportService).exportData(Mockito.any());
        dataExportStatusService.startExport("pass");
        assertThrows(ExportInProgressException.class, () -> dataExportStatusService.startExport("pass"));
        Mockito.verify(dataExportService, Mockito.timeout(1000)).exportData(Mockito.any());
        Thread.sleep(350);
        assertEquals(JobStatus.SUCCESS, dataExportStatusService.getStatus());
        assertEquals("", dataExportStatusService.getMessage());
    }

    @Test
    void testFailedStatus() throws Exception {
        Mockito.doThrow(new RuntimeException("fail")).when(dataExportService).exportData(Mockito.any());
        dataExportStatusService.startExport("pass");
        Mockito.verify(dataExportService, Mockito.timeout(1000)).exportData(Mockito.any());
        Thread.sleep(50);
        assertEquals(JobStatus.FAILED, dataExportStatusService.getStatus());
        assertEquals("fail", dataExportStatusService.getMessage());
    }

    @Test
    void testPasswordErrorStatus() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Password invalid"))
                .when(dataExportService).exportData(Mockito.any());
        dataExportStatusService.startExport("bad");
        Mockito.verify(dataExportService, Mockito.timeout(1000)).exportData(Mockito.any());
        Thread.sleep(50);
        assertEquals(JobStatus.PASSWORD_ERROR, dataExportStatusService.getStatus());
        assertEquals("Password invalid", dataExportStatusService.getMessage());
    }
}
