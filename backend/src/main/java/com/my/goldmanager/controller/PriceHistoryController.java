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

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.my.goldmanager.rest.entity.PriceHistoryList;
import com.my.goldmanager.rest.response.ErrorResponse;
import com.my.goldmanager.service.PriceHistoryService;
import com.my.goldmanager.service.exception.BadRequestException;

@RestController
@RequestMapping("/api/priceHistory")
public class PriceHistoryController {

	@Autowired
	private PriceHistoryService priceHistoryService;

	@GetMapping(path = "/{materialId}")
	public ResponseEntity<PriceHistoryList> listAllforMaterial(@PathVariable("materialId") String materialId,
			@RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") Date startDate,
			@RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") Date endDate) {

		if (startDate != null && endDate != null) {
			if (startDate.after(endDate)) {
				throw new BadRequestException("startDate must be before or equal to endDate");
			}
		}
		Optional<PriceHistoryList> result = priceHistoryService.listAllForMaterial(materialId, startDate, endDate);
		if (result.isPresent()) {
			return ResponseEntity.ok(result.get());
		}

		return ResponseEntity.notFound().build();

	}

	@ExceptionHandler(BadRequestException.class)
	public final ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, WebRequest request) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

}
