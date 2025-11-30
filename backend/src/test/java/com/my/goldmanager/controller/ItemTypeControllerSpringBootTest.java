package com.my.goldmanager.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.my.goldmanager.entity.ItemType;
import com.my.goldmanager.entity.Material;
import com.my.goldmanager.repository.ItemTypeRepository;
import com.my.goldmanager.repository.MaterialRepository;
import com.my.goldmanager.service.AuthenticationService;
import com.my.goldmanager.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ItemTypeControllerSpringBootTest {
	@Autowired
	private UserService userService;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private ItemTypeRepository itemTypeRepository;
	@Autowired
	private MaterialRepository materialRepository;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private Material gold;
	private Material silver;

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
	}

	@AfterEach
	public void cleanUp() {
		itemTypeRepository.deleteAll();
		materialRepository.deleteAll();

		gold = null;
		silver = null;
		TestHTTPClient.cleanup();
	}

	@Test
	public void testList() throws Exception {
		List<ItemType> itemTypes = new LinkedList<ItemType>();

		ItemType goldBar = new ItemType();
		goldBar.setModifier(1.0f);
		goldBar.setMaterial(gold);
		goldBar.setName("1 oz GoldBar");

		itemTypeRepository.save(goldBar);
		itemTypes.add(goldBar);

		ItemType mappleLeaf = new ItemType();
		mappleLeaf.setModifier(0.5f);
		mappleLeaf.setMaterial(gold);
		mappleLeaf.setName("1/2 oz Mapple Leaf");

		itemTypeRepository.save(mappleLeaf);
		itemTypes.add(mappleLeaf);

		ItemType silverBar = new ItemType();
		silverBar.setModifier(1.0f);
		silverBar.setMaterial(silver);
		silverBar.setName("1/2 oz SilverBar");

		itemTypeRepository.save(silverBar);
		itemTypes.add(silverBar);

		String body = mockMvc.perform(TestHTTPClient.doGet("/itemTypes"))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn()
				.getResponse().getContentAsString();

		JsonNode node = objectMapper.readTree(body);
		assertFalse(node.isEmpty());
		assertEquals(itemTypes.size(), node.size());

		for (int current = 0; current < itemTypes.size(); current++) {
			ItemType expected = itemTypes.get(current);

			ItemType result = objectMapper.readValue(node.get(current).toString(), ItemType.class);
			assertNotNull(result);
			assertEquals(expected.getName(), result.getName());
			assertEquals(expected.getModifier(), result.getModifier());

			assertNotNull(result.getMaterial());
			assertEquals(expected.getMaterial().getName(), result.getMaterial().getName());
			assertEquals(expected.getMaterial().getPrice(), result.getMaterial().getPrice());
		}

	}

	@Test
	public void testCreate() throws JacksonException, Exception {
		ItemType mappleLeaf = new ItemType();
		mappleLeaf.setModifier(0.5f);
		mappleLeaf.setMaterial(gold);
		mappleLeaf.setName("1/2 oz Mapple Leaf");

		mockMvc.perform(TestHTTPClient.doPost("/itemTypes").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(mappleLeaf))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value(mappleLeaf.getName()))
				.andExpect(jsonPath("$.modifier").value(mappleLeaf.getModifier()));
	}

	@Test
	public void testDelete() throws JacksonException, Exception {
		ItemType mappleLeaf = new ItemType();
		mappleLeaf.setModifier(0.5f);
		mappleLeaf.setMaterial(gold);
		mappleLeaf.setName("1/2 oz Mapple Leaf");

		mappleLeaf = itemTypeRepository.save(mappleLeaf);

		mockMvc.perform(TestHTTPClient.doDelete("/itemTypes/" + mappleLeaf.getId()))
				.andExpect(status().isNoContent());

		assertFalse(itemTypeRepository.existsById(mappleLeaf.getId()));

		mockMvc.perform(TestHTTPClient.doDelete("/itemTypes/" + mappleLeaf.getId()))
				.andExpect(status().isNotFound());

	}

	@Test
	public void testUpdate() throws JacksonException, Exception {
		ItemType mappleLeaf = new ItemType();
		mappleLeaf.setModifier(0.5f);
		mappleLeaf.setMaterial(gold);
		mappleLeaf.setName("1/2 oz Mapple Leaf");

		mappleLeaf = itemTypeRepository.save(mappleLeaf);

		mappleLeaf.setModifier(1);
		mappleLeaf.setName("1 oz Mapple Leaf");

		mockMvc.perform(TestHTTPClient.doPut("/itemTypes/" + mappleLeaf.getId())
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(mappleLeaf)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.name").value(mappleLeaf.getName()))
				.andExpect(jsonPath("$.modifier").value(mappleLeaf.getModifier()));

	}

	@Test
	public void testGet() throws JacksonException, Exception {
		ItemType mappleLeaf = new ItemType();
		mappleLeaf.setModifier(0.5f);
		mappleLeaf.setMaterial(gold);
		mappleLeaf.setName("1/2 oz Mapple Leaf");

		mappleLeaf = itemTypeRepository.save(mappleLeaf);

		mockMvc.perform(TestHTTPClient.doGet("/itemTypes/" + mappleLeaf.getId()))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.name").value(mappleLeaf.getName()))
				.andExpect(jsonPath("$.modifier").value(mappleLeaf.getModifier()));

		mockMvc.perform(TestHTTPClient.doGet("/itemTypes/notexistent"))
				.andExpect(status().isNotFound());
	}

}
