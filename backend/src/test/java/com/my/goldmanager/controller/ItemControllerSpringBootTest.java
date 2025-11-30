package com.my.goldmanager.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.my.goldmanager.entity.Item;
import com.my.goldmanager.entity.ItemStorage;
import com.my.goldmanager.entity.ItemType;
import com.my.goldmanager.entity.Material;
import com.my.goldmanager.entity.Unit;
import com.my.goldmanager.repository.ItemRepository;
import com.my.goldmanager.repository.ItemStorageRepository;
import com.my.goldmanager.repository.ItemTypeRepository;
import com.my.goldmanager.repository.MaterialRepository;
import com.my.goldmanager.repository.UnitRepository;
import com.my.goldmanager.service.AuthenticationService;
import com.my.goldmanager.service.UserService;
import com.my.goldmanager.service.exception.ValidationException;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ItemControllerSpringBootTest {
	@Autowired
	private UserService userService;
	
	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private ItemTypeRepository itemTypeRepository;

	@Autowired
	private MaterialRepository materialRepository;

	@Autowired
	private UnitRepository unitRepository;

	@Autowired
	private ItemRepository itemRepository;
	
	@Autowired
	private ItemStorageRepository itemStorageRepository;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private Material gold;
	private Material silver;

	private Unit oz;
	private Unit gramm;

	private ItemType goldBar;
	private ItemType silverBar;

	private ItemStorage myItemStorage;
	
	@BeforeEach
	public void setUp() {
		TestHTTPClient.setup(userService,authenticationService);
		Material m = new Material();
		m.setName("Gold");
		m.setPrice(2000);
		gold = materialRepository.save(m);

		m = new Material();
		m.setName("Silver");
		m.setPrice(500);

		silver = materialRepository.save(m);

		oz = new Unit();
		oz.setFactor(1.0f);
		oz.setName("Oz");

		gramm = new Unit();
		gramm.setName("Gramm");
		gramm.setFactor(1 / 0.32f);

		unitRepository.save(oz);
		unitRepository.save(gramm);

		ItemType itemType = new ItemType();
		itemType.setMaterial(gold);
		itemType.setModifier(1.0f);
		itemType.setName("Goldbar 999 Finegold");

		goldBar = itemTypeRepository.save(itemType);

		itemType.setMaterial(silver);
		itemType.setModifier(1.0f);
		itemType.setName("Silverbar 999 Finesilver");

		silverBar = itemTypeRepository.save(itemType);

		myItemStorage = new ItemStorage();
		myItemStorage.setName("MyStorage");
		myItemStorage.setDescription("Test Storage");
		
		myItemStorage = itemStorageRepository.save(myItemStorage);
	}

	@AfterEach
	public void cleanUp() throws ValidationException {
		itemRepository.deleteAll();
		itemTypeRepository.deleteAll();
		materialRepository.deleteAll();
		unitRepository.deleteAll();
		itemStorageRepository.deleteAll();
		gold = null;
		silver = null;
		goldBar = null;
		silverBar = null;
		myItemStorage = null;
		TestHTTPClient.cleanup();
	}

	@Test
	public void testList() throws Exception {
		List<Item> items = new LinkedList<Item>();

		Item goldbar1oz = new Item();
		goldbar1oz.setAmount(1);
		goldbar1oz.setItemCount(1);
		goldbar1oz.setUnit(oz);
		goldbar1oz.setName("1 oz Goldbar");
		goldbar1oz.setItemType(goldBar);
		goldbar1oz.setItemStorage(myItemStorage);
		
		itemRepository.save(goldbar1oz);
		items.add(goldbar1oz);
		
		Item silverbar1oz = new Item();
		silverbar1oz.setAmount(1);
		silverbar1oz.setItemCount(1);
		silverbar1oz.setUnit(oz);
		silverbar1oz.setName("1 oz Silverbar");
		silverbar1oz.setItemType(silverBar);

		itemRepository.save(silverbar1oz);
		items.add(silverbar1oz);

		Item goldbar100gramm = new Item();
		goldbar100gramm.setAmount(100);
		goldbar100gramm.setUnit(gramm);
		goldbar100gramm.setName("100 gramm Goldbar");
		goldbar100gramm.setItemType(goldBar);
		goldbar100gramm.setItemStorage(myItemStorage);
		
		itemRepository.save(goldbar100gramm);
		items.add(goldbar100gramm);

		String body = mockMvc.perform(TestHTTPClient.doGet("/items")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse()
				.getContentAsString();

		JsonNode node = objectMapper.readTree(body);
		assertFalse(node.isEmpty());
		assertEquals(items.size(), node.size());

		for (int current = 0; current < items.size(); current++) {
			Item expected = items.get(current);

			Item result = objectMapper.readValue(node.get(current).toString(), Item.class);
			assertNotNull(result);
			assertEquals(expected.getName(), result.getName());
			assertEquals(expected.getAmount(), result.getAmount());

			assertNotNull(result.getUnit());

			assertEquals(expected.getUnit().getName(), result.getUnit().getName());
			assertEquals(expected.getUnit().getFactor(), result.getUnit().getFactor());

			assertNotNull(result.getItemType());
			assertEquals(expected.getItemType().getName(), result.getItemType().getName());
			assertEquals(expected.getItemType().getModifier(), result.getItemType().getModifier());

			assertNotNull(result.getItemType().getMaterial());

			assertEquals(expected.getItemType().getMaterial().getName(), result.getItemType().getMaterial().getName());
			assertEquals(expected.getItemType().getMaterial().getPrice(),
					result.getItemType().getMaterial().getPrice());
			
			if(expected.getItemStorage() != null) {
				assertEquals(expected.getItemStorage().getId(), result.getItemStorage().getId());
				assertEquals(expected.getItemStorage().getName(), result.getItemStorage().getName());
				assertEquals(expected.getItemStorage().getDescription(), result.getItemStorage().getDescription());
			}
			else {
				assertNull(result.getItemStorage());
			}
			
		}

	}

	@Test
	public void testCreate() throws JacksonException, Exception {
		Item goldBarItem = new Item();
		goldBarItem.setItemType(goldBar);
		goldBarItem.setUnit(oz);
		goldBarItem.setAmount(1);
		goldBarItem.setName("Goldbar 1oz");

		mockMvc.perform(TestHTTPClient.doPost("/items").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(goldBarItem))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value(goldBarItem.getName()))
				.andExpect(jsonPath("$.amount").value(goldBarItem.getAmount()));
	}

	@Test
	public void testDelete() throws JacksonException, Exception {
		Item goldBarItem = new Item();
		goldBarItem.setItemType(goldBar);
		goldBarItem.setUnit(oz);
		goldBarItem.setAmount(1);
		goldBarItem.setName("Goldbar 1oz");

		goldBarItem = itemRepository.save(goldBarItem);

		mockMvc.perform(TestHTTPClient.doDelete("/items/" + goldBarItem.getId()))
				.andExpect(status().isNoContent());

		assertFalse(itemRepository.existsById(goldBarItem.getId()));

		mockMvc.perform(TestHTTPClient.doDelete("/items/" + goldBarItem.getId()))
				.andExpect(status().isNotFound());

	}

	@Test
	public void testUpdate() throws JacksonException, Exception {
		Item goldBarItem = new Item();
		goldBarItem.setItemType(goldBar);
		goldBarItem.setUnit(oz);
		goldBarItem.setAmount(1);
		goldBarItem.setName("Goldbar 1oz");

		goldBarItem = itemRepository.save(goldBarItem);

		goldBarItem.setUnit(gramm);
		goldBarItem.setAmount(100);
		goldBarItem.setName("Goldbar 100 Gramm");

		mockMvc.perform(TestHTTPClient.doPut("/items/" + goldBarItem.getId())
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(goldBarItem)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.name").value(goldBarItem.getName()))
				.andExpect(jsonPath("$.amount").value(goldBarItem.getAmount()));

	}

	@Test
	public void testGet() throws JacksonException, Exception {
		Item goldBarItem = new Item();
		goldBarItem.setItemType(goldBar);
		goldBarItem.setUnit(oz);
		goldBarItem.setAmount(1);
		goldBarItem.setName("Goldbar 1oz");

		goldBarItem = itemRepository.save(goldBarItem);

		mockMvc.perform(TestHTTPClient.doGet("/items/" + goldBarItem.getId()))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.name").value(goldBarItem.getName()))
				.andExpect(jsonPath("$.amount").value(goldBarItem.getAmount()));

		mockMvc.perform(TestHTTPClient.doGet("/items/notexistent"))
				.andExpect(status().isNotFound());
	}

}
