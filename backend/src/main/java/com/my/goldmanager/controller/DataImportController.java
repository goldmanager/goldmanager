package com.my.goldmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.request.WebRequest;

import com.my.goldmanager.rest.request.ImportDataRequest;
import com.my.goldmanager.rest.response.ErrorResponse;
import java.util.UUID;

import com.my.goldmanager.service.ImportJobService;
import com.my.goldmanager.service.entity.JobStatus;
import com.my.goldmanager.service.exception.ImportInProgressException;
import com.my.goldmanager.service.exception.BadRequestException;

@RestController
@RequestMapping("/api/dataimport")
public class DataImportController {

        @Autowired
        private ImportJobService importJobService;

        @PostMapping("/import")
        public ResponseEntity<String> importData(@RequestBody ImportDataRequest importDataRequest) {
                try {
                        UUID jobId = importJobService.startImport(importDataRequest.getData(),
                                        importDataRequest.getPassword());
                        return ResponseEntity.accepted().body(jobId.toString());
                } catch (ImportInProgressException e) {
                        throw e;
                } catch (Exception e) {
                        throw new BadRequestException("Error importing data: " + e.getMessage(), e);
                }
        }

        @GetMapping("/status/{jobId}")
        public ResponseEntity<JobStatus> getStatus(@org.springframework.web.bind.annotation.PathVariable UUID jobId) {
                JobStatus status = importJobService.getStatus(jobId);
                if (status == null) {
                        return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(status);
        }

        @ExceptionHandler(ImportInProgressException.class)
        public final ResponseEntity<ErrorResponse> handleImportInProgressException(ImportInProgressException ex,
                        WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(BadRequestException.class)
        public final ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

}
