/** Copyright 2025 fg12111

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
 * 
 */
package com.my.goldmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.my.goldmanager.rest.request.ExportDataRequest;
import com.my.goldmanager.rest.response.ErrorResponse;
import com.my.goldmanager.service.DataExportService;
import com.my.goldmanager.service.exception.BadRequestException;

@RestController
@RequestMapping("/api/dataexport")
public class DataExportController {
	@Autowired
	private DataExportService dataExportService;

	@PostMapping("/export")
	public ResponseEntity<byte[]> export(@RequestBody ExportDataRequest exportDataRequest) {
		try {
			byte[] data = dataExportService.exportData(exportDataRequest.getPassword());
			return ResponseEntity.ok()
					.header("Content-Type", "application/octet-stream").body(data);
		} catch (Exception e) {
			throw new BadRequestException("Error exporting data: " + e.getMessage(), e);
		}
	}

	@ExceptionHandler(BadRequestException.class)
	public final ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, WebRequest request) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}
}
