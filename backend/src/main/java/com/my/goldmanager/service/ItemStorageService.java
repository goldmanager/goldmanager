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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.my.goldmanager.entity.ItemStorage;
import com.my.goldmanager.repository.ItemStorageRepository;
import com.my.goldmanager.service.exception.ValidationException;

/**
 * Service bean for managing {@link ItemStorage} objects.
 */
@Service
public class ItemStorageService {
	@Autowired
	private ItemStorageRepository itemStorageRepository;

	/**
	 * Stores the provided ItemStorage as new ItemStorage
	 * 
	 * @param itemStorage
	 * @return created Object
	 * @throws ValidationException
	 */
	public ItemStorage create(ItemStorage itemStorage) throws ValidationException {
		validate(itemStorage);
		if (itemStorageRepository.existsByName(itemStorage.getName().trim())) {
			throw new ValidationException("Item Storage name must be unique.");
		}
		ItemStorage toStore = new ItemStorage();

		toStore.setName(itemStorage.getName().trim());
		toStore.setDescription(itemStorage.getDescription());

		return itemStorageRepository.save(toStore);
	}

	public Optional<ItemStorage> update(ItemStorage itemStorage) throws ValidationException {

		if (itemStorageRepository.existsById(itemStorage.getId())) {
			validate(itemStorage);

			ItemStorage toStore = new ItemStorage();
			toStore.setId(itemStorage.getId());
			toStore.setName(itemStorage.getName().trim());
			toStore.setDescription(itemStorage.getDescription());

			toStore = itemStorageRepository.save(toStore);
			return Optional.of(toStore);
		}
		return Optional.empty();
	}

	public boolean deleteById(String id) {
		if (itemStorageRepository.existsById(id)) {
			itemStorageRepository.deleteById(id);
			return true;
		}
		return false;
	}

	public Optional<ItemStorage> getById(String id){
		
		return itemStorageRepository.findById(id);
		
	}
	public List<ItemStorage> listAll() {
		return itemStorageRepository.findAll();
	}

	private void validate(ItemStorage itemStorage) throws ValidationException {
		if (itemStorage.getName() == null || itemStorage.getName().isBlank()) {
			throw new ValidationException("Item Storage name is mandatory.");
		}

	}

}
