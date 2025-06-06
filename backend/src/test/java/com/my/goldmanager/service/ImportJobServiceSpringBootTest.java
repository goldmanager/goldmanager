package com.my.goldmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.my.goldmanager.service.entity.JobStatus;
import com.my.goldmanager.service.exception.ImportInProgressException;

@SpringBootTest
@ActiveProfiles("test")
class ImportJobServiceSpringBootTest {

    @Autowired
    private ImportJobService importJobService;

    @MockitoBean
    private DataImportService dataImportService;

    @Test
    void testStartImportInvokesAsync() throws Exception {
        UUID id = importJobService.startImport("data".getBytes(), "pass");
        Mockito.verify(dataImportService, Mockito.timeout(1000)).importData(Mockito.any(), Mockito.any());
        Thread.sleep(50);
        assertEquals(JobStatus.SUCCESS, importJobService.getStatus(id));
    }

    @Test
    void testStartImportWhileRunningThrows() throws Exception {
        Mockito.doAnswer(i -> {
            Thread.sleep(300);
            return null;
        }).when(dataImportService).importData(Mockito.any(), Mockito.any());
        UUID id = importJobService.startImport("data".getBytes(), "pass");
        assertThrows(ImportInProgressException.class,
                () -> importJobService.startImport("data".getBytes(), "pass"));
        Mockito.verify(dataImportService, Mockito.timeout(1000)).importData(Mockito.any(), Mockito.any());
        Thread.sleep(350);
        assertEquals(JobStatus.SUCCESS, importJobService.getStatus(id));
    }

    @Test
    void testFailedStatus() throws Exception {
        Mockito.doThrow(new RuntimeException("fail")).when(dataImportService).importData(Mockito.any(), Mockito.any());
        UUID id = importJobService.startImport("data".getBytes(), "pass");
        Mockito.verify(dataImportService, Mockito.timeout(1000)).importData(Mockito.any(), Mockito.any());
        Thread.sleep(50);
        assertEquals(JobStatus.FAILED, importJobService.getStatus(id));
    }

    @Test
    void testFailedStatus_WrongPassword() throws Exception {
        Mockito.doThrow(new IllegalArgumentException(
                "Reading of decrypted data failed, maybe the provided password is incorrect?"))
                .when(dataImportService).importData(Mockito.any(), Mockito.any());
        UUID id = importJobService.startImport("data".getBytes(), "bad");
        Mockito.verify(dataImportService, Mockito.timeout(1000)).importData(Mockito.any(), Mockito.any());
        Thread.sleep(50);
        assertEquals(JobStatus.FAILED, importJobService.getStatus(id));
    }
}
