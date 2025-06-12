package com.my.goldmanager.service.dataexpimp;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.goldmanager.entity.TestData;
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
import com.my.goldmanager.service.exception.InvalidHashException;
import com.my.goldmanager.service.exception.VersionLoadingException;

@SpringBootTest
@ActiveProfiles("test")
public class DataExporterSpringBootTest {

	@Autowired
	private DataExporter dataExporter;

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

	private TestData testData = null;


	@BeforeEach
	public void setupTestData() throws IOException {

		try (InputStream in = DataExporterSpringBootTest.class.getResourceAsStream("/testdata.json")) {
			testData = objectMapper.readValue(in, TestData.class);
			storeTestDataToDatabase();
		}
	}

	@AfterEach
	public void cleanUp() {
		testData = null;
		itemRepository.deleteAll();
		itemTypeRepository.deleteAll();
		userLoginRepository.deleteAll();
		itemStorageRepository.deleteAll();
		unitRepository.deleteAll();
		materialHistoryRepository.deleteAll();
		materialRepository.deleteAll();

	}

	private void storeTestDataToDatabase() {
		materialRepository.saveAllAndFlush(testData.getMetals());
		materialHistoryRepository.saveAllAndFlush(testData.getMaterialHistories());
		unitRepository.saveAllAndFlush(testData.getUnits());
		userLoginRepository.saveAllAndFlush(testData.getUsers());
		itemStorageRepository.saveAllAndFlush(testData.getItemStorages());
		itemTypeRepository.saveAllAndFlush(testData.getItemTypes());
		itemRepository.saveAllAndFlush(testData.getItems());
	}

	@Test
	public void testExportSuccess()
			throws ExportDataException, VersionLoadingException, StreamReadException, DatabindException, IOException,
			InvalidAlgorithmException, InvalidHashException {
		ExportData exportData = dataExporter.exportData();
		Assertions.assertNotNull(exportData);
		Assertions.assertEquals(versionInfoService.getVersion().toString(), exportData.getVersion());
		Assertions.assertNotNull(exportData.getExportEntityData());
		Assertions.assertNotNull(exportData.getHash());
		Assertions.assertNotNull(exportData.getHashAlgorithm());
		hashUtil.validateHash(exportData);
		ExportDataAssertions.assertEquals(testData,
				objectMapper.readValue(exportData.getExportEntityData(), ExportEntities.class), true);

	}


}
