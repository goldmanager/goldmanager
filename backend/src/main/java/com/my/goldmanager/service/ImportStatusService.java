package com.my.goldmanager.service;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.my.goldmanager.service.entity.JobStatus;
import com.my.goldmanager.service.exception.ImportInProgressException;
import com.my.goldmanager.service.exception.ValidationException;

@Service
public class ImportStatusService {

    private final AtomicReference<JobStatus> status = new AtomicReference<>(JobStatus.IDLE);
    private final AtomicReference<String> message = new AtomicReference<>("");

    @Autowired
    private DataImportService dataImportService;

    @Autowired
    private ApplicationContext applicationContext;

    public void startImport(byte[] data, String password) {
        if (status.get() == JobStatus.RUNNING) {
            throw new ImportInProgressException("Import already running");
        }
        status.set(JobStatus.RUNNING);
        message.set("");
        applicationContext.getBean(ImportStatusService.class).executeImport(data, password);
    }

    @Async
    void executeImport(byte[] data, String password) {
        try {
            dataImportService.importData(data, password);
            status.set(JobStatus.SUCCESS);
            message.set("");
        } catch (ValidationException | IllegalArgumentException e) {
            status.set(JobStatus.PASSWORD_ERROR);
            message.set(e.getMessage());
        } catch (Exception e) {
            status.set(JobStatus.FAILED);
            message.set(e.getMessage());
        }
    }

    public JobStatus getStatus() {
        return status.get();
    }

    public String getMessage() {
        return message.get();
    }
}
