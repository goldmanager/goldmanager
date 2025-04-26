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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zafarkhaja.semver.Version;
import com.my.goldmanager.repository.ItemRepository;
import com.my.goldmanager.repository.ItemStorageRepository;
import com.my.goldmanager.repository.ItemTypeRepository;
import com.my.goldmanager.repository.MaterialHistoryRepository;
import com.my.goldmanager.repository.MaterialRepository;
import com.my.goldmanager.repository.UnitRepository;
import com.my.goldmanager.repository.UserLoginRepository;
import com.my.goldmanager.service.AuthenticationService;
import com.my.goldmanager.service.VersionInfoService;
import com.my.goldmanager.service.entity.ExportData;
import com.my.goldmanager.service.entity.ExportEntities;
import com.my.goldmanager.service.exception.ImportDataException;
import com.my.goldmanager.service.exception.InvalidAlgorithmException;
import com.my.goldmanager.service.exception.InvalidHashException;
import com.my.goldmanager.service.exception.VersionLoadingException;

@Service
public class DataImporter {

	private static final Logger logger = LoggerFactory.getLogger(DataImporter.class);
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
	private AuthenticationService authenticationService;
	@Autowired
	private HashUtil hashUtil;

	@Value("${com.my.goldmanager.service.dataexpimp.importBatchSize:1000}")
	private int batchSize;

	private final ReentrantLock lock = new ReentrantLock();

	/**
	 * Imports from provided data and revokes all authentication keys
	 * 
	 * @param data
	 */
	@Transactional
	public void importData(ExportData data) throws ImportDataException {
		lock.lock();
		try {
			if (data.getVersion() == null || !Version.isValid(data.getVersion())) {
				throw new ImportDataException("ExportedData version is invalid.");
			}

			Version versionFromData = Version.parse(data.getVersion());
			if (versionFromData == null) {
				throw new ImportDataException("ExportedData version is null.");
			}

			if (!versionFromData.equals(versionInfoService.getVersion())) {
				throw new ImportDataException("Can only import data from the current version. (Expected "
						+ versionInfoService.getVersion().toString() + ", but got " + versionFromData.toString() + ")");
			}
			if (data.getExportEntityData() == null || data.getExportEntityData().length == 0) {
				throw new ImportDataException("Data for import is empty.");
			}
			hashUtil.validateHash(data);
			ExportEntities exportEntities = deserializeEntities(data);
			importEntities(exportEntities);
		} catch (VersionLoadingException e) {
			throw new ImportDataException("Can not verify the version of imported data", e);
		} catch (InvalidAlgorithmException e) {
			throw new ImportDataException("Can not verify the algorithm of imported data", e);
		} catch (InvalidHashException e) {
			throw new ImportDataException("Can not verify the hash of imported data", e);
		} finally {
			lock.unlock();
		}
	}

	private void importEntities(ExportEntities exportEntities) {
		logger.info("Starting entity import...");
		// First step, clear current entities:
		// Data loss is not a problem, because we are going to import all data again
		logger.info("Clearing existing data...");
		itemRepository.deleteAllInBatch();
		itemTypeRepository.deleteAllInBatch();
		itemStorageRepository.deleteAllInBatch();
		userLoginRepository.deleteAllInBatch();
		unitRepository.deleteAllInBatch();
		materialHistoryRepository.deleteAllInBatch();
		materialRepository.deleteAllInBatch();
		logger.info("Existing data cleared.");
		// Second Step logout all users
		logger.info("Logging out all users...");
		authenticationService.logoutAll();

		// Save entities in batches
		logger.info("Saving entities in batches...");
		saveInBatches(userLoginRepository, exportEntities.getUsers(), batchSize);
		saveInBatches(materialRepository, exportEntities.getMetals(), batchSize);
		saveInBatches(unitRepository, exportEntities.getUnits(), batchSize);
		saveInBatches(materialHistoryRepository, exportEntities.getMaterialHistories(), batchSize);
		saveInBatches(itemStorageRepository, exportEntities.getItemStorages(), batchSize);
		saveInBatches(itemTypeRepository, exportEntities.getItemTypes(), batchSize);
		saveInBatches(itemRepository, exportEntities.getItems(), batchSize);
		logger.info("Entity import completed successfully.");
	}

	private <T> void saveInBatches(JpaRepository<T, ?> repository, List<T> entities, int batchSize) {
		for (int i = 0; i < entities.size(); i += batchSize) {
			int end = Math.min(i + batchSize, entities.size());
			List<T> batch = entities.subList(i, end);
			repository.saveAll(batch);
		}
	}

	private ExportEntities deserializeEntities(ExportData data) throws ImportDataException {

		try {
			return objectMapper.readValue(data.getExportEntityData(), ExportEntities.class);
		} catch (IOException e) {
			throw new ImportDataException("Could not deserialize entities for import", e);
		}
	}
}
