package com.my.goldmanager.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
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
import com.my.goldmanager.entity.Unit;
import com.my.goldmanager.repository.UnitRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UnitControllerSpringBootTest {
	@Autowired
	private UnitRepository unitRepository;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@AfterEach
	public void cleanUp() {
		unitRepository.deleteAll();
	}

	@Test
	public void testList() throws Exception {
		Unit gramm = new Unit();
		gramm.setName("Gramm");
		gramm.setFactor(1.0f / 31.1034768f);
		unitRepository.save(gramm);

		Unit oz = new Unit();
		oz.setName("Oz");
		oz.setFactor(1.0f);
		unitRepository.save(oz);

		String body = mockMvc.perform(get("/units")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse()
				.getContentAsString();

		JsonNode node = objectMapper.readTree(body);
		assertFalse(node.isEmpty());
		assertEquals(2, node.size());

		Unit result = objectMapper.readValue(node.get(0).toString(), Unit.class);
		assertNotNull(result);
		assertEquals(gramm.getName(), result.getName());
		assertEquals(gramm.getFactor(), result.getFactor());

		result = objectMapper.readValue(node.get(1).toString(), Unit.class);
		assertNotNull(result);
		assertEquals(oz.getName(), result.getName());
		assertEquals(oz.getFactor(), result.getFactor());

	}

	@Test
	public void testCreate() throws JsonProcessingException, Exception {
		Unit gramm = new Unit();
		gramm.setName("gramm");
		gramm.setFactor(1.0f / 31.1034768f);
		mockMvc.perform(
				post("/units").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(gramm)))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("gramm"))
				.andExpect(jsonPath("$.factor").value(gramm.getFactor()));
	}

	@Test
	public void testUpdate() throws JsonProcessingException, Exception {
		Unit gramm = new Unit();
		gramm.setName("gramm");
		gramm.setFactor(1.0f);

		Unit created = unitRepository.save(gramm);
		created.setFactor(0.5f);

		mockMvc.perform(put("/units/" + created.getName()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(created))).andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("gramm")).andExpect(jsonPath("$.factor").value(0.5));

	}

	@Test
	public void testDelete() throws JsonProcessingException, Exception {
		Unit unit = new Unit();
		unit.setName("OZ");
		unit.setFactor(1);
		unitRepository.save(unit);
		mockMvc.perform(delete("/units/{id}", unit.getName())).andExpect(status().isNoContent());

		assertFalse(unitRepository.existsById(unit.getName()));

		mockMvc.perform(delete("/units/{id}", unit.getName())).andExpect(status().isNotFound());

	}

}