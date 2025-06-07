package com.my.goldmanager.service.dataexpimp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zafarkhaja.semver.Version;
import com.my.goldmanager.entity.TestData;
import com.my.goldmanager.repository.ItemRepository;
import com.my.goldmanager.repository.ItemStorageRepository;
import com.my.goldmanager.repository.ItemTypeRepository;
import com.my.goldmanager.repository.MaterialHistoryRepository;
import com.my.goldmanager.repository.MaterialRepository;
import com.my.goldmanager.repository.UnitRepository;
import com.my.goldmanager.repository.UserLoginRepository;
import com.my.goldmanager.service.AuthKeyInfoService;
import com.my.goldmanager.service.AuthenticationService;
import com.my.goldmanager.service.UserService;
import com.my.goldmanager.service.VersionInfoService;
import com.my.goldmanager.service.entity.ExportData;
import com.my.goldmanager.service.entity.KeyInfo;
import com.my.goldmanager.service.exception.ImportDataException;
import com.my.goldmanager.service.exception.InvalidAlgorithmException;
import com.my.goldmanager.service.exception.ValidationException;
import com.my.goldmanager.service.exception.VersionLoadingException;

@SpringBootTest
@ActiveProfiles("test")
public class DataImporterSpringBootTest {

	@Autowired
	private DataImporter dataImporter;

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
	private AuthKeyInfoService authKeyInfoService;

	@Autowired
	private UserService userService;

	@Autowired
	private HashUtil hashUtil;

	private ExportData testData;
	private TestData expectedData;

	@AfterEach
	public void cleanup() {

		authenticationService.logoutAll();
		userLoginRepository.deleteAll();
		itemRepository.deleteAll();
		itemStorageRepository.deleteAll();
		itemTypeRepository.deleteAll();
		unitRepository.deleteAll();
		materialHistoryRepository.deleteAll();
		materialRepository.deleteAll();

	}

	/**
	 * This method is used to store the test data to the database. It is called
	 * before each test.
	 * 
	 * @throws IOException
	 * @throws InvalidAlgorithmException
	 */
	@BeforeEach
	public void setupTestData() throws IOException, VersionLoadingException, InvalidAlgorithmException {

		try (InputStream in = DataExporterSpringBootTest.class.getResourceAsStream("/importdata.dat")) {
			testData = new ExportData();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			in.transferTo(bos);
			testData.setExportEntityData(bos.toByteArray());
			testData.setVersion(versionInfoService.getVersion().toString());
			hashUtil.hashData(testData);

		}
		try (InputStream in = DataExporterSpringBootTest.class.getResourceAsStream("/testdata.json")) {
			expectedData = objectMapper.readValue(in, TestData.class);

		}

	}

	/**
	 * This method is used to store the expected data to the database. It is called
	 * before each test.
	 */
	private void storeExpectedDataToDatabase() {
		materialRepository.saveAllAndFlush(expectedData.getMetals());
		materialHistoryRepository.saveAllAndFlush(expectedData.getMaterialHistories());
		unitRepository.saveAllAndFlush(expectedData.getUnits());
		userLoginRepository.saveAllAndFlush(expectedData.getUsers());
		itemStorageRepository.saveAllAndFlush(expectedData.getItemStorages());
		itemTypeRepository.saveAllAndFlush(expectedData.getItemTypes());
		itemRepository.saveAllAndFlush(expectedData.getItems());
	}

	@Test
	/**
	 * This test is used to test the import data method. It is called after the
	 * expected data is stored in the database.
	 * 
	 * @throws ImportDataException
	 */
	void testSuccessOnNonEmptyDB() throws ImportDataException {
		// Store the expected data to the database
		storeExpectedDataToDatabase();

		// Import the data
		dataImporter.importData(testData);

		// Verify the imported data using the repositories
		assertEquals(expectedData.getMetals().size(), materialRepository.count());
		assertEquals(expectedData.getMaterialHistories().size(), materialHistoryRepository.count());
		assertEquals(expectedData.getUnits().size(), unitRepository.count());
		assertEquals(expectedData.getItemStorages().size(), itemStorageRepository.count());
		assertEquals(expectedData.getUsers().size(), userLoginRepository.count());
		assertEquals(expectedData.getItemTypes().size(), itemTypeRepository.count());
		assertEquals(expectedData.getItems().size(), itemRepository.count());

	}

	@Test

	void testFailureCorruptImportData() throws VersionLoadingException, InvalidAlgorithmException {
		// Corrupt the import data
		testData.setExportEntityData(new byte[] { 0x00, 0x01, 0x02 });
		hashUtil.hashData(testData);
		testData.setVersion(versionInfoService.getVersion().toString());
		// Import the data
		ImportDataException ex = assertThrows(ImportDataException.class, () -> {
			dataImporter.importData(testData);
		});
		assertEquals("Could not deserialize entities for import", ex.getMessage());
	}

	@Test
	void testFailureOnWrongVersion() throws VersionLoadingException {

		Version version = versionInfoService.getVersion().nextMinorVersion();

		testData.setVersion(version.toString());
		// Import the data
		ImportDataException ex = assertThrows(ImportDataException.class, () -> {
			dataImporter.importData(testData);
		});
		assertEquals("Can only import data from the current version. (Expected "
				+ versionInfoService.getVersion().toString() + ", but got " + version.toString() + ")",
				ex.getMessage());

	}
	@Test
	void testFailureOnInvalidHash() throws VersionLoadingException {
		// Corrupt the import data
		testData.setHash(new byte[] { 0x00, 0x01, 0x02 });

		// Import the data
		ImportDataException ex = assertThrows(ImportDataException.class, () -> {
			dataImporter.importData(testData);
		});
		assertEquals("Can not verify the hash of imported data", ex.getMessage());
	}

	@Test
	void testFailureOnEmptyImportData() throws VersionLoadingException {
		// Corrupt the import data
		testData.setExportEntityData(new byte[] {});
		testData.setVersion(versionInfoService.getVersion().toString());
		// Import the data
		ImportDataException ex = assertThrows(ImportDataException.class, () -> {
			dataImporter.importData(testData);
		});
		assertEquals("Data for import is empty.", ex.getMessage());
	}

	@Test
	void testFailureOnEmptyVersion() throws VersionLoadingException {
		// Corrupt the import data
		testData.setExportEntityData(new byte[] {});
		testData.setVersion("");
		// Import the data
		ImportDataException ex = assertThrows(ImportDataException.class, () -> {
			dataImporter.importData(testData);
		});
		assertEquals("ExportedData version is invalid.", ex.getMessage());
	}

	@Test
	void testFailureOnNullImportData() throws VersionLoadingException {
		// Corrupt the import data
		testData.setExportEntityData(null);
		testData.setVersion(versionInfoService.getVersion().toString());
		// Import the data
		ImportDataException ex = assertThrows(ImportDataException.class, () -> {
			dataImporter.importData(testData);
		});
		assertEquals("Data for import is empty.", ex.getMessage());
	}

	@Test
	void testFailureOnNullVersion() throws VersionLoadingException {
		// Corrupt the import data
		testData.setExportEntityData(new byte[] {});
		testData.setVersion(null);
		// Import the data
		ImportDataException ex = assertThrows(ImportDataException.class, () -> {
			dataImporter.importData(testData);
		});
		assertEquals("ExportedData version is invalid.", ex.getMessage());
	}

	@Test
	void testPreExistingUserDeleted() throws ImportDataException, ValidationException {
		userService.create("MyUserABC", "Uhu123456!");

		authenticationService.getJWTToken("MyUserABC", "Uhu123456!");

		// Verify that the user is logged in
		KeyInfo keyInfo = authKeyInfoService.getKeyInfoForUserName("MyUserABC");
		assertNotNull(keyInfo);

		// Import the data
		dataImporter.importData(testData);
		// Verify that the pre-existing user is deleted
		assertFalse(userLoginRepository.existsById("MyUserABC"));

		// Verify that the user is logged out
		assertNull(authKeyInfoService.getKeyforKeyId(keyInfo.getKeyId()));

	}
	@Test
	void testUserLogout() throws ImportDataException {
		// Store the expected data to the database
		storeExpectedDataToDatabase();

		authenticationService.getJWTToken("user1", "Test1245");

		// Verify that the user is logged in
		KeyInfo keyInfo = authKeyInfoService.getKeyInfoForUserName(expectedData.getUsers().get(0).getUserid());
		assertNotNull(keyInfo);

		// Import the data
		dataImporter.importData(testData);

		// Verify that the user is logged out
		assertNull(authKeyInfoService.getKeyforKeyId(keyInfo.getKeyId()));

	}

	@Test
	void testSuccessOnEmptyDB() throws ImportDataException {
		// Import the data
		dataImporter.importData(testData);

		// Verify the imported data using the repositories
		assertEquals(expectedData.getMetals().size(), materialRepository.count());
		assertEquals(expectedData.getMaterialHistories().size(), materialHistoryRepository.count());
		assertEquals(expectedData.getUnits().size(), unitRepository.count());
		assertEquals(expectedData.getItemStorages().size(), itemStorageRepository.count());
		assertEquals(expectedData.getUsers().size(), userLoginRepository.count());
		assertEquals(expectedData.getItemTypes().size(), itemTypeRepository.count());
		assertEquals(expectedData.getItems().size(), itemRepository.count());

		expectedData.getMetals().forEach(expectedMaterial -> {
			assertTrue(materialRepository.findById(expectedMaterial.getId()).isPresent());
			assertEquals(expectedMaterial.getPrice(),
					materialRepository.findById(expectedMaterial.getId()).get().getPrice());
			assertEquals(expectedMaterial.getName(),
					materialRepository.findById(expectedMaterial.getId()).get().getName());
			assertEquals(expectedMaterial.getEntryDate().getTime(),
					materialRepository.findById(expectedMaterial.getId()).get().getEntryDate().getTime());
		});
		expectedData.getMaterialHistories().forEach(expectedMaterialHistory -> {
			assertTrue(materialHistoryRepository.findById(expectedMaterialHistory.getId()).isPresent());
			assertEquals(expectedMaterialHistory.getMaterial().getId(),
					materialHistoryRepository.findById(expectedMaterialHistory.getId()).get().getMaterial().getId());
			assertEquals(expectedMaterialHistory.getEntryDate().getTime(),
					materialHistoryRepository.findById(expectedMaterialHistory.getId()).get().getEntryDate().getTime());
			assertEquals(expectedMaterialHistory.getPrice(),
					materialHistoryRepository.findById(expectedMaterialHistory.getId()).get().getPrice());
		});
		expectedData.getUnits().forEach(expectedUnit -> {
			assertTrue(unitRepository.findById(expectedUnit.getName()).isPresent());
			assertEquals(expectedUnit.getName(), unitRepository.findById(expectedUnit.getName()).get().getName());
			assertEquals(expectedUnit.getFactor(), unitRepository.findById(expectedUnit.getName()).get().getFactor());
		});
		expectedData.getItemStorages().forEach(expectedItemStorage -> {
			assertTrue(itemStorageRepository.findById(expectedItemStorage.getId()).isPresent());
			assertEquals(expectedItemStorage.getName(),
					itemStorageRepository.findById(expectedItemStorage.getId()).get().getName());
			assertEquals(expectedItemStorage.getDescription(),
					itemStorageRepository.findById(expectedItemStorage.getId()).get().getDescription());

		});
		expectedData.getUsers().forEach(expectedUser -> {
			assertTrue(userLoginRepository.findById(expectedUser.getUserid()).isPresent());
			assertEquals(expectedUser.getPassword(),
					userLoginRepository.findById(expectedUser.getUserid()).get().getPassword());
		});
		expectedData.getItemTypes().forEach(expectedItemType -> {
			assertTrue(itemTypeRepository.findById(expectedItemType.getId()).isPresent());
			assertEquals(expectedItemType.getMaterial().getId(),
					itemTypeRepository.findById(expectedItemType.getId()).get().getMaterial().getId());
			assertEquals(expectedItemType.getName(),
					itemTypeRepository.findById(expectedItemType.getId()).get().getName());
			assertEquals(expectedItemType.getModifier(),
					itemTypeRepository.findById(expectedItemType.getId()).get().getModifier());
		});
		expectedData.getItems().forEach(expectedItem -> {
			assertTrue(itemRepository.findById(expectedItem.getId()).isPresent());
			if (expectedItem.getItemStorage() != null) {
				assertEquals(expectedItem.getItemStorage().getId(),
						itemRepository.findById(expectedItem.getId()).get().getItemStorage().getId());
			} else {
				assertNull(itemRepository.findById(expectedItem.getId()).get().getItemStorage());
			}
			assertEquals(expectedItem.getItemType().getId(),
					itemRepository.findById(expectedItem.getId()).get().getItemType().getId());
			assertEquals(expectedItem.getName(), itemRepository.findById(expectedItem.getId()).get().getName());
			assertEquals(expectedItem.getAmount(), itemRepository.findById(expectedItem.getId()).get().getAmount());
			assertEquals(expectedItem.getItemCount(),
					itemRepository.findById(expectedItem.getId()).get().getItemCount());

		});

	}
}
