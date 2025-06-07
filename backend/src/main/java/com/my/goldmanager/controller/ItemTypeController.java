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

import com.my.goldmanager.entity.ItemType;
import com.my.goldmanager.rest.response.ErrorResponse;
import com.my.goldmanager.service.ItemTypeService;
import com.my.goldmanager.service.exception.BadRequestException;

@RestController()
@RequestMapping("/api/itemTypes")
public class ItemTypeController {

	@Autowired
	private ItemTypeService itemTypeService;

	/**
	 * Create {@link ItemType}
	 * 
	 * @param itemType
	 * @return
	 */
	@PostMapping
	public ResponseEntity<ItemType> create(@RequestBody ItemType itemType) {
		try {
			ItemType savedItemType = itemTypeService.create(itemType);
			return ResponseEntity.status(HttpStatus.CREATED).body(savedItemType);
		} catch (Exception e) {
			throw new BadRequestException(e.getMessage(), e);
		}
	}

	/**
	 * Updates provided {@link ItemType}
	 * 
	 * @param id
	 * @param itemType
	 * @return
	 */
	@PutMapping(path = "/{id}")
	public ResponseEntity<ItemType> update(@PathVariable(name = "id") String id, @RequestBody ItemType itemType) {
		try {
			Optional<ItemType> result = itemTypeService.update(id, itemType);
			if (result.isPresent()) {
				return ResponseEntity.ok(result.get());
			}
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			throw new BadRequestException(e.getMessage(), e);
		}
	}

	/**
	 * Returns ItemType by Id
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping(path = "/{id}")
	public ResponseEntity<ItemType> get(@PathVariable(name = "id") String id) {
		Optional<ItemType> result = itemTypeService.getById(id);
		if (result.isPresent()) {
			return ResponseEntity.ok(result.get());
		}
		return ResponseEntity.notFound().build();
	}

	/**
	 * Delete ItemType by id
	 * 
	 * @param id
	 * @return
	 */
	@DeleteMapping(path = "/{id}")
	public ResponseEntity<Void> delete(@PathVariable(name = "id") String id) {
		if (itemTypeService.delete(id)) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.notFound().build();
	}

	/**
	 * List all Item types
	 * 
	 * @return
	 */
	@GetMapping
	public List<ItemType> list() {
		return itemTypeService.list();
	}

	@ExceptionHandler(BadRequestException.class)
	public final ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, WebRequest request) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}
}
