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

import com.my.goldmanager.entity.Unit;
import com.my.goldmanager.rest.response.ErrorResponse;
import com.my.goldmanager.service.UnitService;
import com.my.goldmanager.service.exception.BadRequestException;
import com.my.goldmanager.service.exception.ValidationException;

@RestController
@RequestMapping("/api/units")
public class UnitController {

	@Autowired
	private UnitService unitService;

	@GetMapping
	public List<Unit> getAll() {
		return unitService.listAll();
	}

	@GetMapping(path = "/{name}")
	public ResponseEntity<Unit> getByName(@PathVariable(name = "name") String name) {
		Optional<Unit> optional = unitService.getByName(name);
		if (optional.isPresent()) {
			return ResponseEntity.ok(optional.get());
		}
		return ResponseEntity.notFound().build();
	}

	@PostMapping
	public ResponseEntity<Unit> create(@RequestBody Unit unit) {
		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(unitService.save(unit));
		} catch (ValidationException ve) {
			throw new BadRequestException(ve.getMessage(), ve);
		}
	}

	@PutMapping(path = "/{name}")
	public ResponseEntity<Unit> update(@PathVariable(name = "name") String name, @RequestBody Unit unit) {
		try {
			Optional<Unit> optionalUnit = unitService.update(name, unit);
			if (optionalUnit.isPresent()) {
				return ResponseEntity.ok(optionalUnit.get());
			}
			return ResponseEntity.notFound().build();
		} catch (ValidationException ve) {
			throw new BadRequestException(ve.getMessage(), ve);
		}
	}

	@DeleteMapping(path = "/{name}")
	public ResponseEntity<Void> delete(@PathVariable(name = "name") String name) {
		if (unitService.deleteByName(name)) {
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
