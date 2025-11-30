package com.my.goldmanager.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
import com.my.goldmanager.rest.entity.Price;
import com.my.goldmanager.rest.entity.PriceGroup;
import com.my.goldmanager.rest.entity.PriceGroupList;
import com.my.goldmanager.rest.entity.PriceList;
import com.my.goldmanager.service.AuthenticationService;
import com.my.goldmanager.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PriceControllerSpringBootTest {

	@Autowired
	private UserService userService;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private ItemStorageRepository itemStorageRepository;

	@Autowired
	private ItemTypeRepository itemTypeRepository;
	@Autowired
	private MaterialRepository materialRepository;

	@Autowired
	private UnitRepository unitRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private Material gold;
	private Material silver;

	private Unit oz;
	private Unit gramm;

	List<Item> items;

	private static PriceGroup getPriceGroup(String groupName, PriceGroupList priceGroupList) {
		return priceGroupList.getPriceGroups().stream().filter(pg -> groupName.equals(pg.getGroupName())).distinct()
				.findFirst().orElseThrow();
	}

	@BeforeEach
	public void setUp() {
		TestHTTPClient.setup(userService, authenticationService);

		ItemStorage storage1 = new ItemStorage();
		storage1.setName("storage1");
		storage1.setDescription("description1");
		storage1 = itemStorageRepository.save(storage1);

		ItemStorage storage2 = new ItemStorage();
		storage2.setName("storage2");
		storage2.setDescription("description2");
		storage2 = itemStorageRepository.save(storage2);

		Material m = new Material();
		m.setName("Gold");
		m.setPrice(2000);
		gold = materialRepository.save(m);

		m = new Material();
		m.setName("Silver");
		m.setPrice(30);

		silver = materialRepository.save(m);

		oz = new Unit();
		oz.setFactor(1.0f);
		oz.setName("Oz");

		gramm = new Unit();
		gramm.setName("Gramm");
		gramm.setFactor(1.0f / 0.32f);

		unitRepository.save(oz);
		unitRepository.save(gramm);

		ItemType fineGold999 = new ItemType();
		fineGold999.setMaterial(gold);
		fineGold999.setModifier(1.0f);
		fineGold999.setName("Bar Finegold 0.999");
		fineGold999 = itemTypeRepository.save(fineGold999);

		ItemType gold585 = new ItemType();

		gold585.setMaterial(gold);
		gold585.setModifier(0.585f / 0.999f);
		gold585.setName("Bar Gold 0.585");
		gold585 = itemTypeRepository.save(gold585);

		ItemType fineSilver999 = new ItemType();
		fineSilver999.setMaterial(silver);
		fineSilver999.setModifier(1);
		fineSilver999.setName("FineSilver");

		fineSilver999 = itemTypeRepository.save(fineSilver999);

		items = new LinkedList<Item>();

		Item item = new Item();
		item.setName("1 OZ Bar");
		item.setItemType(fineGold999);
		item.setAmount(1);
		item.setItemCount(2);
		item.setUnit(oz);
		item.setItemStorage(storage1);
		
		item = itemRepository.save(item);
		items.add(item);

		item = new Item();
		item.setName("1/2 OZ Bar");
		item.setItemType(fineGold999);
		item.setAmount(0.5f);
		item.setItemCount(3);
		item.setUnit(oz);
		item.setItemStorage(storage1);
		item = itemRepository.save(item);
		items.add(item);

		item = new Item();
		item.setName("20g Bar");
		item.setItemType(fineGold999);
		item.setAmount(20);
		item.setUnit(gramm);
		item.setItemStorage(storage2);
		item = itemRepository.save(item);
		
		items.add(item);

		item = new Item();
		item.setName("5g Bar");
		item.setItemType(gold585);
		item.setAmount(5);
		item.setUnit(gramm);
		item.setItemStorage(storage2);
		item = itemRepository.save(item);
		items.add(item);

		item = new Item();
		item.setName("500g Bar");
		item.setItemType(fineSilver999);
		item.setAmount(500);
		item.setUnit(gramm);
		item.setItemStorage(storage2);
		item = itemRepository.save(item);
		items.add(item);

	}

	@Test
	void testListAll() throws UnsupportedEncodingException, Exception {

		String body = mockMvc.perform(TestHTTPClient.doGet("/prices")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse()
				.getContentAsString();

		PriceList result = objectMapper.readValue(body, PriceList.class);
		assertNotNull(result);
		assertEquals(items.size(), result.getPrices().size());
		assertEquals(getPriceSummary(items), result.getTotalPrice());

		for (int current = 0; current < items.size(); current++) {
			Item expected = items.get(current);
			Price price = result.getPrices().get(current);
			assertEquals(getSingleItemPrice(expected), price.getPrice());
			assertEquals(getTotalItemPrice(expected), price.getPriceTotal());
			assertEquals(expected.getName(), price.getItem().getName());
			assertEquals(expected.getId(), price.getItem().getId());
			assertEquals(expected.getItemStorage().getId(), price.getItem().getItemStorage().getId());
			assertEquals(expected.getItemStorage().getName(), price.getItem().getItemStorage().getName());
			assertEquals(expected.getItemStorage().getDescription(), price.getItem().getItemStorage().getDescription());
		}

	}

	@Test
	void testGroupByItemtype() throws UnsupportedEncodingException, Exception {

		String body = mockMvc.perform(TestHTTPClient.doGet("/prices/groupBy/itemType")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse()
				.getContentAsString();

		PriceGroupList result = objectMapper.readValue(body, PriceGroupList.class);
		assertNotNull(result);
		assertEquals(Long.valueOf(itemTypeRepository.count()).intValue(), result.getPriceGroups().size());
		List<ItemType> itemtypes = itemTypeRepository.findAll();
		for (ItemType itemtype : itemtypes) {
			List<Item> expectedItems = items.stream()
					.filter(item -> item.getItemType().getId().equals(itemtype.getId())).toList();

			PriceGroup actualPriceGroup = getPriceGroup(itemtype.getName(), result);
			assertEquals(expectedItems.size(), actualPriceGroup.getPrices().size());
			assertEquals(getAmmount(expectedItems), actualPriceGroup.getAmount());
			assertEquals(getPriceSummary(expectedItems), actualPriceGroup.getTotalPrice());

			for (int current = 0; current < expectedItems.size(); current++) {
				Item expected = expectedItems.get(current);
				Price actual = actualPriceGroup.getPrices().get(current);
				assertEquals(getSingleItemPrice(expected), actual.getPrice());
				assertEquals(getTotalItemPrice(expected), actual.getPriceTotal());
				assertEquals(expected.getName(), actual.getItem().getName());
				assertEquals(expected.getItemStorage().getId(), actual.getItem().getItemStorage().getId());
				assertEquals(expected.getItemStorage().getName(), actual.getItem().getItemStorage().getName());
				assertEquals(expected.getItemStorage().getDescription(), actual.getItem().getItemStorage().getDescription());
			}
		}

	}

	@Test
	void testGroupByMaterial() throws UnsupportedEncodingException, Exception {

		String body = mockMvc.perform(TestHTTPClient.doGet("/prices/groupBy/material")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse()
				.getContentAsString();

		PriceGroupList result = objectMapper.readValue(body, PriceGroupList.class);
		assertNotNull(result);
		assertEquals(2, result.getPriceGroups().size());

		List<Item> goldItems = items.stream()
				.filter(item -> item.getItemType().getMaterial().getId().equals(gold.getId())).toList();
		List<Item> silverItems = items.stream()
				.filter(item -> item.getItemType().getMaterial().getId().equals(silver.getId())).toList();

		PriceGroup goldPriceGroup = getPriceGroup(gold.getName(), result);
		assertEquals(goldItems.size(), goldPriceGroup.getPrices().size());
		assertEquals(getAmmount(goldItems), goldPriceGroup.getAmount());
		assertEquals(getPriceSummary(goldItems), goldPriceGroup.getTotalPrice());

		for (int current = 0; current < goldItems.size(); current++) {
			Item expected = goldItems.get(current);
			Price actual = goldPriceGroup.getPrices().get(current);
			assertEquals(getSingleItemPrice(expected), actual.getPrice());
			assertEquals(getTotalItemPrice(expected), actual.getPriceTotal());
			assertEquals(expected.getName(), actual.getItem().getName());
			assertEquals(expected.getItemStorage().getId(), actual.getItem().getItemStorage().getId());
			assertEquals(expected.getItemStorage().getName(), actual.getItem().getItemStorage().getName());
			assertEquals(expected.getItemStorage().getDescription(), actual.getItem().getItemStorage().getDescription());
		}
		PriceGroup silverPriceGroup = getPriceGroup(silver.getName(), result);
		assertEquals(silverItems.size(), silverPriceGroup.getPrices().size());
		assertEquals(getAmmount(silverItems), silverPriceGroup.getAmount());
		assertEquals(getPriceSummary(silverItems), silverPriceGroup.getTotalPrice());

		for (int current = 0; current < silverItems.size(); current++) {
			Item expected = silverItems.get(current);
			Price actual = silverPriceGroup.getPrices().get(current);
			assertEquals(getSingleItemPrice(expected), actual.getPrice());
			assertEquals(expected.getName(), actual.getItem().getName());
			assertEquals(expected.getItemStorage().getId(), actual.getItem().getItemStorage().getId());
			assertEquals(expected.getItemStorage().getName(), actual.getItem().getItemStorage().getName());
			assertEquals(expected.getItemStorage().getDescription(), actual.getItem().getItemStorage().getDescription());
		}

	}

	@Test
	void testlistPricesForMaterialGold() throws UnsupportedEncodingException, Exception {

		String body = mockMvc.perform(TestHTTPClient.doGet("/prices/material/" + gold.getId()))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn()
				.getResponse().getContentAsString();

		PriceList result = objectMapper.readValue(body, PriceList.class);
		assertNotNull(result);

		List<Item> goldItems = items.stream()
				.filter(item -> item.getItemType().getMaterial().getId().equals(gold.getId())).toList();
		assertEquals(goldItems.size(), result.getPrices().size());
		assertEquals(getPriceSummary(goldItems), result.getTotalPrice());

		for (int current = 0; current < goldItems.size(); current++) {
			Item expected = goldItems.get(current);
			Price price = result.getPrices().get(current);
			assertEquals(getSingleItemPrice(expected), price.getPrice());
			assertEquals(expected.getName(), price.getItem().getName());
			assertEquals(expected.getId(), price.getItem().getId());
			assertEquals(expected.getItemStorage().getId(), price.getItem().getItemStorage().getId());
			assertEquals(expected.getItemStorage().getName(), price.getItem().getItemStorage().getName());
			assertEquals(expected.getItemStorage().getDescription(), price.getItem().getItemStorage().getDescription());
		}

	}

	@Test
	void testlistPricesForMaterialSilver() throws UnsupportedEncodingException, Exception {

		String body = mockMvc.perform(TestHTTPClient.doGet("/prices/material/" + silver.getId()))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn()
				.getResponse().getContentAsString();

		PriceList result = objectMapper.readValue(body, PriceList.class);
		assertNotNull(result);

		List<Item> goldItems = items.stream()
				.filter(item -> item.getItemType().getMaterial().getId().equals(silver.getId())).toList();
		assertEquals(goldItems.size(), result.getPrices().size());
		assertEquals(getPriceSummary(goldItems), result.getTotalPrice());

		for (int current = 0; current < goldItems.size(); current++) {
			Item expected = goldItems.get(current);
			Price price = result.getPrices().get(current);
			assertEquals(getSingleItemPrice(expected), price.getPrice());
			assertEquals(getTotalItemPrice(expected), price.getPriceTotal());
			assertEquals(expected.getName(), price.getItem().getName());
			assertEquals(expected.getId(), price.getItem().getId());
			assertEquals(expected.getItemStorage().getId(), price.getItem().getItemStorage().getId());
			assertEquals(expected.getItemStorage().getName(), price.getItem().getItemStorage().getName());
			assertEquals(expected.getItemStorage().getDescription(), price.getItem().getItemStorage().getDescription());
		}

	}

	@Test
	void testlistPricesForMaterialUnknown() throws UnsupportedEncodingException, Exception {

		mockMvc.perform(TestHTTPClient.doGet("/prices/material/unknown")).andExpect(status().isNotFound());
	}

	@Test
	void testlistPricesForItemStorage() throws UnsupportedEncodingException, Exception {

		List<ItemStorage> itemStorages = itemStorageRepository.findAll();
		
		for(ItemStorage storage: itemStorages) {
			String body = mockMvc.perform(TestHTTPClient.doGet("/prices/itemStorage/" + storage.getId()))
					.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn()
					.getResponse().getContentAsString();

			PriceList result = objectMapper.readValue(body, PriceList.class);
			assertNotNull(result);
			List<Item> expectedItems= items.stream().filter(items-> items.getItemStorage().getId().equals(storage.getId())).collect(Collectors.toList());
			assertEquals(expectedItems.size(), result.getPrices().size());
			assertEquals(getPriceSummary(expectedItems), result.getTotalPrice());
			for(int i=0; i < expectedItems.size(); i++) {
				Item expected = expectedItems.get(i);
				Price price = result.getPrices().get(i);
				assertEquals(getTotalItemPrice(expected), price.getPriceTotal());
				assertEquals(getSingleItemPrice(expected), price.getPrice());
				assertEquals(getTotalItemPrice(expected), price.getPriceTotal());
				assertEquals(expected.getName(), price.getItem().getName());
				assertEquals(expected.getId(), price.getItem().getId());
				assertEquals(expected.getItemStorage().getId(), price.getItem().getItemStorage().getId());
				assertEquals(expected.getItemStorage().getName(), price.getItem().getItemStorage().getName());
				assertEquals(expected.getItemStorage().getDescription(), price.getItem().getItemStorage().getDescription());
			}
		}
	
		

	}
	@Test
	void testlistPricesForItemStorageUnknown() throws UnsupportedEncodingException, Exception {

		mockMvc.perform(TestHTTPClient.doGet("/prices/itemStorage/unknown")).andExpect(status().isNotFound());
	}
	
	@Test
	void testGetByItem() throws UnsupportedEncodingException, Exception {

		for (int current = 0; current < items.size(); current++) {
			Item expected = items.get(current);
			String body = mockMvc.perform(TestHTTPClient.doGet("/prices/item/" + expected.getId()))
					.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn()
					.getResponse().getContentAsString();

			Price result = objectMapper.readValue(body, Price.class);
			assertNotNull(result);

			assertEquals(getSingleItemPrice(expected), result.getPrice());
			assertEquals(getTotalItemPrice(expected), result.getPriceTotal());
			assertEquals(expected.getName(), result.getItem().getName());
			assertEquals(expected.getId(), result.getItem().getId());
		}
		mockMvc.perform(TestHTTPClient.doGet("/prices/item/notexistent")).andExpect(status().isNotFound());

	}

	private float getAmmount(List<Item> items) {
		float result = 0;
		for (Item item : items) {
			result += Float.valueOf(item.getItemCount()) * item.getAmount() * item.getUnit().getFactor();
		}
		BigDecimal bd = new BigDecimal(result).setScale(2, RoundingMode.HALF_DOWN);
		return bd.floatValue();
	}

	private float getPriceSummary(List<Item> items) {
		float result = 0;
		for (Item item : items) {
			result += getTotalItemPrice(item);
		}
		return new BigDecimal(result).setScale(2, RoundingMode.HALF_DOWN).floatValue();
	}

	private float getSingleItemPrice(Item item) {
		BigDecimal price = new BigDecimal(item.getAmount() * item.getUnit().getFactor()
				* item.getItemType().getModifier() * item.getItemType().getMaterial().getPrice())
				.setScale(2, RoundingMode.HALF_DOWN);
		return price.floatValue();
	}

	private float getTotalItemPrice(Item item) {
		BigDecimal price = new BigDecimal(
				Float.valueOf(item.getItemCount()) * item.getAmount() * item.getUnit().getFactor()
						* item.getItemType().getModifier() * item.getItemType().getMaterial().getPrice())
				.setScale(2, RoundingMode.HALF_DOWN);
		return price.floatValue();
	}

	@AfterEach
	public void cleanUp() {
		itemRepository.deleteAll();
		itemTypeRepository.deleteAll();
		materialRepository.deleteAll();
		unitRepository.deleteAll();
		itemStorageRepository.deleteAll();
		gold = null;
		silver = null;
		items.clear();
		TestHTTPClient.cleanup();
	}

}
