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

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.my.goldmanager.entity.Material;
import com.my.goldmanager.entity.MaterialHistory;
import com.my.goldmanager.repository.MaterialHistoryRepository;
import com.my.goldmanager.repository.MaterialRepository;
import com.my.goldmanager.service.exception.ValidationException;
@Service
public class MaterialService {
	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
			.withZone(ZoneId.of("UTC"));
	private static final long MAX_ENTRY_DATE_GRACE_PERIOD = 60 * 1000;

	@Autowired
	private MaterialRepository materialRepository;

	@Autowired
	private MaterialHistoryRepository materialHistoryRepository;

	@Transactional
	public Material store(Material material) throws ValidationException {
		validateMaterial(material, true);
		material.setId(null);
		material = materialRepository.save(material);
		saveMaterialHistory(material);
		return material;
	}

	@Transactional
	public Optional<Material> update(String id, Material material) throws ValidationException {
		Optional<Material> oldOptional = materialRepository.findById(id);
		if (oldOptional.isPresent()) {
			Material old = oldOptional.get();
			validateMaterial(material, false, old);
			material = materialRepository.save(material);
			saveMaterialHistory(material);
			return Optional.of(material);
		}
		return Optional.empty();
	}

	private void validateMaterial(Material material, boolean isNew) throws ValidationException {
		validateMaterial(material, isNew, null);
	}

	private void validateMaterial(Material material, boolean isNew, Material old) throws ValidationException {
		if (material.getName() == null || material.getName().isBlank()) {
			throw new ValidationException("Material name is mandatory.");
		}
		if (material.getPrice() <= 0) {
			throw new ValidationException("Price must be greater than 0.");
		}
		if (material.getEntryDate() == null) {
			material.setEntryDate(new Date());
		} else if (material.getEntryDate().after(new Date(System.currentTimeMillis() + MAX_ENTRY_DATE_GRACE_PERIOD))) {
			throw new ValidationException("EntryDate must not be in the future.");
		}
		if (!isNew && old != null && material.getEntryDate().before(old.getEntryDate())) {
			throw new ValidationException("EntryDate must be after " + formatDateToUTC(old.getEntryDate()));
		}
	}

	private void saveMaterialHistory(Material material) {
		if (material == null) {
			throw new IllegalArgumentException("Material cannot be null.");
		}
		MaterialHistory mh = new MaterialHistory();
		mh.setPrice(material.getPrice());
		mh.setEntryDate(material.getEntryDate());
		mh.setMaterial(material);
		materialHistoryRepository.save(mh);
	}

	@Transactional(readOnly = true)
	public List<Material> list() {
		return materialRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Optional<Material> getById(String id) {
		return materialRepository.findById(id);
	}

	@Transactional
	public boolean deleteById(String id) {
		if (materialRepository.existsById(id)) {
			materialRepository.deleteById(id);
			return true;
		}
		return false;
	}

	private static String formatDateToUTC(Date date) {
		return dtf.format(date.toInstant()) + "+00:00";
	}
}
