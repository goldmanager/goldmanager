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

import com.my.goldmanager.rest.request.ImportDataRequest;
import com.my.goldmanager.rest.response.ErrorResponse;
import com.my.goldmanager.service.DataImportService;
import com.my.goldmanager.service.exception.BadRequestException;

@RestController
@RequestMapping("/api/dataimport")
public class DataImportController {

	@Autowired
	private DataImportService dataImportService;

	@PostMapping("/import")
	public ResponseEntity<String> importData(@RequestBody ImportDataRequest importDataRequest) {
		try {
			dataImportService.importData(importDataRequest.getData(), importDataRequest.getPassword());
			return ResponseEntity.noContent().build();
		} catch (Exception e) {
			throw new BadRequestException("Error importing data: " + e.getMessage(), e);
		}
	}

	@ExceptionHandler(BadRequestException.class)
	public final ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, WebRequest request) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

}
