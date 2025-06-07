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

import com.my.goldmanager.entity.ItemStorage;
import com.my.goldmanager.rest.response.ErrorResponse;
import com.my.goldmanager.service.ItemStorageService;
import com.my.goldmanager.service.exception.BadRequestException;

@RestController
@RequestMapping("/api/itemStorages")
/**
 * Rest API controller for ItemStorages
 */
public class ItemStorageController {

	@Autowired
	private ItemStorageService itemStorageService;

	/**
	 * Create the provided {@link ItemStorage}.
	 * 
	 * @param itemStorage
	 * @return
	 */
	@PostMapping
	public ResponseEntity<ItemStorage> create(@RequestBody ItemStorage itemStorage) {
		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(itemStorageService.create(itemStorage));
		} catch (Exception e) {
			throw new BadRequestException(e.getMessage(), e);
		}
	}

	/**
	 * Lists all ItemStorages
	 * 
	 * @return
	 */
	@GetMapping
	public List<ItemStorage> list() {
		return itemStorageService.listAll();
	}

	/**
	 * Returns {@link ItemStorage} by Id
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping(path = "/{id}")
	public ResponseEntity<ItemStorage> getById(@PathVariable(name = "id") String id) {
		Optional<ItemStorage> result = itemStorageService.getById(id);
		if (result.isPresent()) {
			return ResponseEntity.ok(result.get());
		}
		return ResponseEntity.notFound().build();
	}

	/**
	 * Delete ItemStorage by Id
	 * 
	 * @param id
	 * @return
	 */
	@DeleteMapping(path = "/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") String id) {
		if (itemStorageService.deleteById(id)) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.notFound().build();
	}

	/**
	 * Update the given ItemStorage
	 * @param id
	 * @param itemStorage
	 * @return
	 */
	@PutMapping(path = "/{id}")
	public ResponseEntity<ItemStorage> update(@PathVariable("id") String id, @RequestBody ItemStorage itemStorage) {
		itemStorage.setId(id);
		try {
			Optional<ItemStorage> result = itemStorageService.update(itemStorage);
			if (result.isPresent()) {
				return ResponseEntity.ok(result.get());
			}
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			throw new BadRequestException(e.getMessage(), e);
		}
	}

	@ExceptionHandler(BadRequestException.class)
	public final ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, WebRequest request) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}
}
