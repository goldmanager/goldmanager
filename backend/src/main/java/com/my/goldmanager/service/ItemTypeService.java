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
package com.my.goldmanager.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.my.goldmanager.entity.ItemType;
import com.my.goldmanager.repository.ItemTypeRepository;
import com.my.goldmanager.service.exception.DuplicateItemTypeException;
import com.my.goldmanager.service.exception.ValidationException;

@Service
public class ItemTypeService {
	private static final Logger logger = LoggerFactory.getLogger(ItemTypeService.class);
	@Autowired
	private ItemTypeRepository repository;
	@Transactional
	public ItemType create(ItemType itemType) throws ValidationException {
		validateItemTypeName(itemType);
		try {
			logger.info("Creating a new ItemType with name: {}", itemType.getName());
			return repository.save(itemType);
		} catch (DataIntegrityViolationException e) {
			logger.error("Failed to create ItemType. Duplicate name: {}", itemType.getName(), e);
			throw new DuplicateItemTypeException("An item type with the same name already exists.", e);
		}
	}

	@Transactional
	public Optional<ItemType> update(String id, ItemType itemType) throws ValidationException {
		if (repository.existsById(id)) {
			validateItemTypeName(itemType);
			try {
				itemType.setId(id);
				logger.info("Updating ItemType with id: {}", id);
				return Optional.of(repository.save(itemType));
			} catch (DataIntegrityViolationException e) {
				logger.error("Failed to update ItemType with id: {}. Duplicate name: {}", id, itemType.getName(), e);
				throw new DuplicateItemTypeException("An item type with the same name already exists.", e);
			}
		}
		logger.warn("ItemType with id: {} not found for update.", id);
		return Optional.empty();
	}

	private void validateItemTypeName(ItemType itemType) throws ValidationException {
		if (itemType.getName() == null || itemType.getName().isBlank()) {
			throw new ValidationException("Item type name is mandatory.");
		}
	}
	@Transactional(readOnly = true)
	public Optional<ItemType> getById(String id) {
		return repository.findById(id);
	}

	@Transactional(readOnly = true)
	public List<ItemType> list() {
		return repository.findAll();
	}

	@Transactional
	public boolean delete(String id) {
		if (repository.existsById(id)) {
			repository.deleteById(id);
			return true;
		}
		return false;
	}
}
