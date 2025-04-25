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
package com.my.goldmanager.service.dataexpimp;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.goldmanager.entity.Item;
import com.my.goldmanager.entity.ItemStorage;
import com.my.goldmanager.entity.ItemType;
import com.my.goldmanager.entity.Material;
import com.my.goldmanager.entity.MaterialHistory;
import com.my.goldmanager.repository.ItemRepository;
import com.my.goldmanager.repository.ItemStorageRepository;
import com.my.goldmanager.repository.ItemTypeRepository;
import com.my.goldmanager.repository.MaterialHistoryRepository;
import com.my.goldmanager.repository.MaterialRepository;
import com.my.goldmanager.repository.UnitRepository;
import com.my.goldmanager.repository.UserLoginRepository;
import com.my.goldmanager.service.VersionInfoService;
import com.my.goldmanager.service.entity.ExportData;
import com.my.goldmanager.service.entity.ExportEntities;
import com.my.goldmanager.service.exception.ExportDataException;
import com.my.goldmanager.service.exception.InvalidAlgorithmException;
import com.my.goldmanager.service.exception.VersionLoadingException;

@Service
public class DataExporter {



	@Autowired
	private MaterialRepository materialRepository;
	@Autowired
	private MaterialHistoryRepository materialHistoryRepository;
	@Autowired
	private UnitRepository unitRepository;
	@Autowired
	private ItemStorageRepository itemStorageRepository;
	@Autowired
	private UserLoginRepository userLoginRepository;
	@Autowired
	private ItemTypeRepository itemTypeRepository;
	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private VersionInfoService versionInfoService;

	@Autowired
	private HashUtil hashUtil;

	/**
	 * Exports Data from Database
	 * 
	 * @return Exported Data
	 * @throws ExportDataException
	 * 
	 */
	@Transactional(readOnly = true)
	public ExportData exportData() throws ExportDataException {
		ExportData result = new ExportData();
		try {
			result.setVersion(versionInfoService.getVersion().toString());

			ExportEntities exportEntities = new ExportEntities();
			exportEntities.setMetals(materialRepository.findAll());
			exportEntities.setUnits(unitRepository.findAll());
			exportEntities.setItemStorages(itemStorageRepository.findAll());
			exportEntities.setUsers(userLoginRepository.findAll());
			exportEntities.setMaterialHistories(simplifyMaterialHistories(materialHistoryRepository.findAll()));
			exportEntities.setItemTypes(simplifyItemTypes(itemTypeRepository.findAll()));
			exportEntities.setItems(simplifyItems(itemRepository.findAll()));
			result.setExportEntityData(objectMapper.writeValueAsBytes(exportEntities));
			hashUtil.hashData(result);
			return result;
		} catch (VersionLoadingException e) {
			throw new ExportDataException("Can not load version information for exported data.", e);
		} catch (JsonProcessingException e) {
			throw new ExportDataException("Serialisation of exported entities has failed", e);
		} catch (InvalidAlgorithmException e) {
			throw new ExportDataException("Hash generation for exported data has failed", e);
		}

	}

	private static List<ItemType> simplifyItemTypes(List<ItemType> in) {
		return in.stream().map((it) -> {
			Material m = new Material();
			m.setId(it.getMaterial().getId());
			it.setMaterial(m);
			return it;
		}).collect(Collectors.toList());
	}

	private static List<MaterialHistory> simplifyMaterialHistories(List<MaterialHistory> in) {
		return in.stream().map((mh) -> {
			Material m = new Material();
			m.setId(mh.getMaterial().getId());
			mh.setMaterial(m);
			return mh;
		}).collect(Collectors.toList());
	}

	private static List<Item> simplifyItems(List<Item> in) {
		return in.stream().map((item) -> {
			ItemType it = new ItemType();
			it.setId(item.getItemType().getId());
			item.setItemType(it);
			if (item.getItemStorage() != null) {
				ItemStorage itemStorage = new ItemStorage();
				itemStorage.setId(item.getItemStorage().getId());
				item.setItemStorage(itemStorage);
			}
			return item;
		}).collect(Collectors.toList());
	}
}
