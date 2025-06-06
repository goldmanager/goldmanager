package com.my.goldmanager.service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.my.goldmanager.service.entity.JobStatus;
import com.my.goldmanager.service.exception.ImportInProgressException;

@Service
public class ImportJobService {

    private final AtomicBoolean importRunning = new AtomicBoolean(false);
    private final ConcurrentHashMap<UUID, JobStatus> jobStatus = new ConcurrentHashMap<>();

    @Autowired
    private DataImportService dataImportService;

    @Autowired
    private ApplicationContext applicationContext;

    public UUID startImport(byte[] data, String password) {
        if (importRunning.get()) {
            throw new ImportInProgressException("Import already running");
        }
        importRunning.set(true);
        UUID jobId = UUID.randomUUID();
        jobStatus.put(jobId, JobStatus.RUNNING);
        applicationContext.getBean(ImportJobService.class).executeImport(jobId, data, password);
        return jobId;
    }

    @Async
    void executeImport(UUID jobId, byte[] data, String password) {
        try {
            dataImportService.importData(data, password);
            jobStatus.put(jobId, JobStatus.SUCCESS);
        } catch (Exception e) {
            jobStatus.put(jobId, JobStatus.FAILED);
        } finally {
            importRunning.set(false);
        }
    }

    public JobStatus getStatus(UUID jobId) {
        return jobStatus.get(jobId);
    }
}
