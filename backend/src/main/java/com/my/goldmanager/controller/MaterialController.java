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

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.my.goldmanager.entity.Material;
import com.my.goldmanager.rest.response.ErrorResponse;
import com.my.goldmanager.service.MaterialService;
import com.my.goldmanager.service.exception.BadRequestException;
import com.my.goldmanager.service.exception.ValidationException;

@RestController
@RequestMapping("/api/materials")
public class MaterialController {
	private static final Logger logger = LoggerFactory.getLogger(MaterialController.class);
	@Autowired
	private MaterialService materialService;

	@PostMapping
	public ResponseEntity<Material> create(@RequestBody Material material) {
		try {
			Material savedmaterial = materialService.store(material);
			return ResponseEntity.status(HttpStatus.CREATED).body(savedmaterial);
		} catch (ValidationException ve) {
			throw new BadRequestException(ve.getMessage(), ve);
		}
	}

	@GetMapping
	public List<Material> list() {
		return materialService.list();
	}

	@GetMapping(path = "/{id}")
	public ResponseEntity<Material> getbyId(@PathVariable(name = "id") String id) {
		Optional<Material> result = materialService.getById(id);
		if (result.isPresent()) {
			return ResponseEntity.ok(result.get());
		}
		return ResponseEntity.notFound().build();

	}

	@PutMapping(path = "/{id}")
	public ResponseEntity<Material> update(@PathVariable(name = "id") String id, @RequestBody Material material) {
		try {
			Optional<Material> result = materialService.update(id, material);
			if (result.isPresent()) {
				return ResponseEntity.ok(result.get());
			}
			return ResponseEntity.notFound().build();
		} catch (ValidationException ve) {
			logger.error("Validation error during updating Material", ve);
			throw new BadRequestException(ve.getMessage(), ve);
		}
	}

	@DeleteMapping(path = "/{id}")
	public ResponseEntity<Void> delete(@PathVariable(name = "id") String id) {
		if (materialService.deleteById(id)) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@ExceptionHandler(BadRequestException.class)
	public final ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, WebRequest request) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}
}
