package com.my.goldmanager.service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.my.goldmanager.service.entity.JobStatus;
import com.my.goldmanager.service.exception.ImportInProgressException;

@Service
public class ImportStatusService {

    private final AtomicBoolean importRunning = new AtomicBoolean(false);
    private final AtomicReference<JobStatus> status = new AtomicReference<>(JobStatus.IDLE);

    @Autowired
    private DataImportService dataImportService;

    @Autowired
    private ApplicationContext applicationContext;

    public void startImport(byte[] data, String password) {
        if (importRunning.get()) {
            throw new ImportInProgressException("Import already running");
        }
        importRunning.set(true);
        status.set(JobStatus.RUNNING);
        applicationContext.getBean(ImportStatusService.class).executeImport(data, password);
    }

    @Async
    void executeImport(byte[] data, String password) {
        try {
            dataImportService.importData(data, password);
            status.set(JobStatus.SUCCESS);
        } catch (Exception e) {
            status.set(JobStatus.FAILED);
        } finally {
            importRunning.set(false);
        }
    }

    public JobStatus getStatus() {
        return status.get();
    }
}
