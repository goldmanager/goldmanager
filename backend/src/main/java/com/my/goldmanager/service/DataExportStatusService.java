package com.my.goldmanager.service;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.my.goldmanager.service.entity.JobStatus;
import com.my.goldmanager.service.exception.ExportInProgressException;

@Service
public class DataExportStatusService {
    private final AtomicReference<JobStatus> status = new AtomicReference<>(JobStatus.IDLE);
    private final AtomicReference<byte[]> data = new AtomicReference<>();

    @Autowired
    private DataExportService dataExportService;

    @Autowired
    private ApplicationContext applicationContext;

    public void startExport(String password) {
        if (status.get() == JobStatus.RUNNING) {
            throw new ExportInProgressException("Export already running");
        }
        status.set(JobStatus.RUNNING);
        data.set(null);
        applicationContext.getBean(DataExportStatusService.class).executeExport(password);
    }

    @Async
    void executeExport(String password) {
        try {
            byte[] result = dataExportService.exportData(password);
            data.set(result);
            status.set(JobStatus.SUCCESS);
        } catch (Exception e) {
            status.set(JobStatus.FAILED);
        }
    }

    public JobStatus getStatus() {
        return status.get();
    }

    public byte[] getData() {
        return data.get();
    }
}
