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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.my.goldmanager.entity.MaterialHistory;
import com.my.goldmanager.repository.MaterialHistoryRepository;
import com.my.goldmanager.service.exception.ValidationException;

@Service
public class MaterialHistoryService {

	@Autowired
	private MaterialHistoryRepository repository;

	@Transactional(readOnly = true)
	public List<MaterialHistory> getAll() {
		return repository.findAll();
	}

	@Transactional(readOnly = true)
	public Optional<MaterialHistory> getbyId(String id) {
		return repository.findById(id);
	}

	@Transactional(readOnly = true)
	public List<MaterialHistory> findAllByMaterial(String materialId) {
		return repository.findByMaterial(materialId);
	}

	@Transactional
	public boolean deleteById(String id) {
		if (repository.existsById(id)) {
			repository.deleteById(id);
			repository.flush();
			return true;
		}
		return false;
	}

	@Transactional
	public void deleteAllByMaterial(String materialId) {
		repository.flush();
		repository.deleteAllInBatch(findAllByMaterial(materialId));
		repository.flush();
	}

	/**
	 * Deletes MaterialHistory in provided date range.
	 * 
	 * @param materialID
	 * @param startDate
	 * @ @throws ValidationException
	 */
	@Transactional
	public void deleteByMaterialAndDateRange(String materialID, Date startDate, Date endDate)
			throws ValidationException {


		if (startDate != null && endDate != null && startDate.after(endDate)) {
			throw new ValidationException("startDate must be before endDate");
		}
		List<MaterialHistory> result = new LinkedList<>();
		if (startDate == null) {
			result = repository.findByMaterialEndAt(materialID, endDate);
		} else if (endDate == null) {
			result = repository.findByMaterialStartAt(materialID, startDate);
		} else {
			result = repository.findByMaterialInRange(materialID, startDate, endDate);
		}
		repository.deleteAllInBatch(result);
		repository.flush();
	}
}
