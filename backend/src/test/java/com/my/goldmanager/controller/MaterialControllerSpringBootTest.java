package com.my.goldmanager.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.goldmanager.entity.Material;
import com.my.goldmanager.entity.MaterialHistory;
import com.my.goldmanager.repository.MaterialHistoryRepository;
import com.my.goldmanager.repository.MaterialRepository;
import com.my.goldmanager.rest.response.ErrorResponse;
import com.my.goldmanager.service.AuthenticationService;
import com.my.goldmanager.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MaterialControllerSpringBootTest {
	@Autowired
	private UserService userService;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private MaterialRepository materialRepository;

	@Autowired
	private MaterialHistoryRepository materialHistoryRepository;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		TestHTTPClient.setup(userService, authenticationService);
	}

	@AfterEach
	void cleanUp() {
		materialHistoryRepository.deleteAll();
		materialRepository.deleteAll();
		TestHTTPClient.cleanup();
	}

	@Test
	void testList() throws Exception {
		Material gold = new Material();
		gold.setName("gold");
		gold.setPrice(100.1f);

		materialRepository.save(gold);

		Material silver = new Material();
		silver.setName("silver");
		silver.setPrice(50.1f);
		materialRepository.save(silver);

		String body = mockMvc.perform(TestHTTPClient.doGet("/materials")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse()
				.getContentAsString();

		JsonNode node = objectMapper.readTree(body);
		assertFalse(node.isEmpty());
		assertEquals(2, node.size());

		Material result = objectMapper.readValue(node.get(0).toString(), Material.class);
		assertNotNull(result);
		assertEquals(gold.getName(), result.getName());
		assertEquals(gold.getPrice(), result.getPrice());

		result = objectMapper.readValue(node.get(1).toString(), Material.class);
		assertNotNull(result);
		assertEquals(silver.getName(), result.getName());
		assertEquals(silver.getPrice(), result.getPrice());

	}

	@Test
	void testGetById() throws Exception {
		Material gold = new Material();
		gold.setName("gold");
		gold.setPrice(100.1f);
		gold.setEntryDate(new Date());

		gold = materialRepository.save(gold);
		mockMvc.perform(TestHTTPClient.doGet("/materials/" + gold.getId())).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("gold")).andExpect(jsonPath("$.price").value(100.1f))
				.andExpect(jsonPath("$.entryDate").value(formatDateToUTC(gold.getEntryDate())));

	}

	@Test
	void testGetByIdNotFound() throws Exception {
		mockMvc.perform(TestHTTPClient.doGet("/materials/unknownid")).andExpect(status().isNotFound());
	}


	@Test
	void testCreateInvalidDate() throws JsonProcessingException, Exception {
		Material material = new Material();
		material.setName("gold");
		material.setPrice(100.1f);
		material.setEntryDate(new Date(System.currentTimeMillis()+61*1000));
		String body = mockMvc
				.perform(TestHTTPClient.doPost("/materials").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(material)))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();

		
		ErrorResponse error = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals(400, error.getStatus());
		assertEquals("EntryDate must not be in the future.", error.getMessage());
	}


	@Test
	void testCreate() throws JsonProcessingException, Exception {
		Material material = new Material();
		material.setName("gold");
		material.setPrice(100.1f);
		String body = mockMvc
				.perform(TestHTTPClient.doPost("/materials").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(material)))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("gold"))
				.andExpect(jsonPath("$.price").value(100.1f)).andReturn().getResponse().getContentAsString();

		Material created = objectMapper.readValue(body, Material.class);
		List<MaterialHistory> mh = materialHistoryRepository.findByMaterial(created.getId());

		assertEquals(1, mh.size());
		MaterialHistory mh1 = mh.get(0);
		assertEquals(created.getPrice(), mh1.getPrice());
		assertEquals(created.getEntryDate().toInstant().toEpochMilli(), mh1.getEntryDate().toInstant().toEpochMilli());
	}

	@Test
	void testCreatePriceSmall() throws JsonProcessingException, Exception {
		Material material = new Material();
		material.setName("gold");
		material.setPrice(0.01f);
		String body = mockMvc
				.perform(TestHTTPClient.doPost("/materials").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(material)))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("gold"))
				.andExpect(jsonPath("$.price").value(0.01f)).andReturn().getResponse().getContentAsString();

		Material created = objectMapper.readValue(body, Material.class);
		List<MaterialHistory> mh = materialHistoryRepository.findByMaterial(created.getId());

		assertEquals(1, mh.size());
		MaterialHistory mh1 = mh.get(0);
		assertEquals(created.getPrice(), mh1.getPrice());
		assertEquals(created.getEntryDate().toInstant().toEpochMilli(), mh1.getEntryDate().toInstant().toEpochMilli());
	}

	@Test
	void testCreatePriceZero() throws JsonProcessingException, Exception {
		Material material = new Material();
		material.setName("gold");
		material.setPrice(0);
		String body = mockMvc
				.perform(TestHTTPClient.doPost("/materials").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(material)))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();

		ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals(400, errorResponse.getStatus());
		assertEquals("Price must be greater than 0.", errorResponse.getMessage());
	}

	@Test
	void testCreatePriceNegative() throws JsonProcessingException, Exception {
		Material material = new Material();
		material.setName("gold");
		material.setPrice(-0.01f);
		String body = mockMvc
				.perform(TestHTTPClient.doPost("/materials").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(material)))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();

		ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals(400, errorResponse.getStatus());
		assertEquals("Price must be greater than 0.", errorResponse.getMessage());
	}

	@Test
	void testCreateAndUpdateWithMaterialHistory() throws JsonProcessingException, UnsupportedEncodingException, Exception {
		Material material = new Material();
		material.setName("gold");
		material.setPrice(100.1f);
		
		
		String body = mockMvc
				.perform(TestHTTPClient.doPost("/materials").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(material)))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		
		material = objectMapper.readValue(body, Material.class);
		List<Material> expected = new LinkedList<>();
		
		expected.add(material);
		
		Material updated = new Material();
		updated.setId(material.getId());
		updated.setName(material.getName());
		updated.setPrice(200);
		body = mockMvc
				.perform(TestHTTPClient.doPut("/materials/" + updated.getId()).contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updated)))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();
		
		expected.add(objectMapper.readValue(body, Material.class));

		updated.setPrice(300);
		body = mockMvc
				.perform(TestHTTPClient.doPut("/materials/" + updated.getId()).contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updated)))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();
		
		expected.add(objectMapper.readValue(body, Material.class));
		List<MaterialHistory> mh = materialHistoryRepository.findByMaterial(material.getId()).reversed();

		assertEquals(expected.size(), mh.size());
		
		for (int i = 0; i < expected.size();i++) {
			Material expectedMaterial = expected.get(i);
			MaterialHistory materialHistory = mh.get(i);
			
			assertEquals(expectedMaterial.getName(), materialHistory.getMaterial().getName());
			assertEquals(expectedMaterial.getPrice(), materialHistory.getPrice());
			assertEquals(expectedMaterial.getEntryDate(), materialHistory.getEntryDate());
		};
	}
	
	@Test
	void testDelete() throws JsonProcessingException, Exception {
		Material material = new Material();
		material.setName("gold");
		material.setPrice(100.1f);
		material = materialRepository.save(material);

		mockMvc.perform(TestHTTPClient.doDelete("/materials/" + material.getId())).andExpect(status().isNoContent());

		assertFalse(materialRepository.existsById(material.getId()));

		mockMvc.perform(TestHTTPClient.doDelete("/materials/" + material.getId())).andExpect(status().isNotFound());

	}

	@Test
	void testDeleteWithistory() throws JsonProcessingException, Exception {
		Material material = new Material();
		material.setName("gold");
		material.setPrice(100.1f);
		material = materialRepository.save(material);

		MaterialHistory mh = new MaterialHistory();
		mh.setEntryDate(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		mh.setMaterial(material);
		mh.setPrice(50f);

		materialHistoryRepository.save(mh);

		mockMvc.perform(TestHTTPClient.doDelete("/materials/" + material.getId())).andExpect(status().isNoContent());

		assertFalse(materialRepository.existsById(material.getId()));

		mockMvc.perform(TestHTTPClient.doDelete("/materials/" + material.getId())).andExpect(status().isNotFound());

	}
	@Test
	void testUpdateDateInFuture() throws JsonProcessingException, Exception {
		Material material = new Material();
		material.setName("gold");
		material.setPrice(100.1f);
		Date createddate = new Date(System.currentTimeMillis());

		material.setEntryDate(createddate);

		Material created = materialRepository.save(material);
		created.setPrice(200);
		created.setEntryDate(new Date(System.currentTimeMillis() + 61*1000));

		String body = mockMvc
				.perform(TestHTTPClient.doPut("/materials/" + created.getId()).contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(created)))
				.andExpect(status().isBadRequest()).andReturn()
				.getResponse().getContentAsString();

		ErrorResponse error = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals(400, error.getStatus());
		assertEquals("EntryDate must not be in the future.", error.getMessage());

	}

	@Test
	void testUpdatePriceZero() throws JsonProcessingException, Exception {
		Material material = new Material();
		material.setName("gold");
		material.setPrice(100.1f);

		Material created = materialRepository.save(material);
		created.setPrice(0);
		created.setEntryDate(null);

		String body = mockMvc
				.perform(TestHTTPClient.doPut("/materials/" + created.getId()).contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(created)))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();

		ErrorResponse error = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals(400, error.getStatus());
		assertEquals("Price must be greater than 0.", error.getMessage());

	}

	@Test
	void testUpdatePriceNegative() throws JsonProcessingException, Exception {
		Material material = new Material();
		material.setName("gold");
		material.setPrice(100.1f);

		Material created = materialRepository.save(material);
		created.setPrice(-0.01f);
		created.setEntryDate(null);

		String body = mockMvc
				.perform(TestHTTPClient.doPut("/materials/" + created.getId()).contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(created)))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();

		ErrorResponse error = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals(400, error.getStatus());
		assertEquals("Price must be greater than 0.", error.getMessage());

	}

	@Test
	void testUpdatePriceSmall() throws JsonProcessingException, Exception {
		Material material = new Material();
		material.setName("gold");
		material.setPrice(100.1f);
		Date createddate = new Date(System.currentTimeMillis());

		material.setEntryDate(createddate);

		Material created = materialRepository.save(material);
		created.setPrice(0.01f);
		created.setEntryDate(new Date(System.currentTimeMillis() + 1000));

		String body = mockMvc
				.perform(TestHTTPClient.doPut("/materials/" + created.getId()).contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(created)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.name").value("gold"))
				.andExpect(jsonPath("$.price").value(0.01f))
				.andExpect(jsonPath("$.entryDate").value(formatDateToUTC(created.getEntryDate()))).andReturn()
				.getResponse().getContentAsString();

		created = objectMapper.readValue(body, Material.class);
		List<MaterialHistory> mh = materialHistoryRepository.findByMaterial(created.getId());

		assertEquals(1, mh.size());
		MaterialHistory mh1 = mh.get(0);
		assertEquals(created.getPrice(), mh1.getPrice());
		assertEquals(created.getEntryDate(), mh1.getEntryDate());

	}

	@Test
	void testUpdateWithInvalidDate() throws JsonProcessingException, Exception {
		Material material = new Material();
		material.setName("gold");
		material.setPrice(100.1f);
		long timeStampNow = System.currentTimeMillis();
		Date createddate = new Date(timeStampNow);

		material.setEntryDate(createddate);

		Material created = materialRepository.save(material);
		created.setPrice(200);
		Date currentDate = new Date(timeStampNow - 5000);
		created.setEntryDate(currentDate);

		String body = mockMvc
				.perform(TestHTTPClient.doPut("/materials/" + created.getId()).contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(created)))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();

		ErrorResponse resp = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals(400, resp.getStatus());
		assertEquals("EntryDate must be after " + formatDateToUTC(createddate), resp.getMessage());
		List<MaterialHistory> mh = materialHistoryRepository.findByMaterial(created.getId());

		assertEquals(0, mh.size());

	}

	@Test
	void testUpdateWithoutNewDate() throws JsonProcessingException, Exception {
		Material material = new Material();
		material.setName("gold");
		material.setPrice(100.1f);
		Date createddate = new Date(System.currentTimeMillis() - 1000);

		material.setEntryDate(createddate);

		Material created = materialRepository.save(material);
		created.setPrice(200);
		Date currentDate = new Date(System.currentTimeMillis());
		created.setEntryDate(null);

		String body = mockMvc
				.perform(TestHTTPClient.doPut("/materials/" + created.getId()).contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(created)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.name").value("gold"))
				.andExpect(jsonPath("$.price").value(200)).andReturn().getResponse().getContentAsString();

		created = objectMapper.readValue(body, Material.class);
		List<MaterialHistory> mh = materialHistoryRepository.findByMaterial(created.getId());

		assertEquals(1, mh.size());
		MaterialHistory mh1 = mh.get(0);
		assertEquals(created.getPrice(), mh1.getPrice());
		assertEquals(created.getEntryDate(), mh1.getEntryDate());

		Material result = materialRepository.findById(created.getId()).get();
		assertTrue(currentDate.toInstant().toEpochMilli() <= result.getEntryDate().toInstant().toEpochMilli());

	}

	static String formatDateToUTC(Date date) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").withZone(ZoneId.of("UTC"));
		return dtf.format(date.toInstant()) + "+00:00";
	}
}
