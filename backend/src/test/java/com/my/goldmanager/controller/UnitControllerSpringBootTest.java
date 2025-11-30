package com.my.goldmanager.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.my.goldmanager.entity.Unit;
import com.my.goldmanager.repository.UnitRepository;

import com.my.goldmanager.rest.response.ErrorResponse;
import com.my.goldmanager.service.AuthenticationService;
import com.my.goldmanager.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UnitControllerSpringBootTest {

	@Autowired
	private UserService userService;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private UnitRepository unitRepository;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	public void setup() {
		TestHTTPClient.setup(userService, authenticationService);
	}

	@AfterEach
	public void cleanUp() {
		unitRepository.deleteAll();
		TestHTTPClient.cleanup();
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

		String body = mockMvc.perform(TestHTTPClient.doGet("/units")).andExpect(status().isOk())
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
	public void testCreate() throws JacksonException, Exception {
		Unit gramm = new Unit();
		gramm.setName("gramm");
		gramm.setFactor(1.0f / 31.1034768f);

		mockMvc.perform(TestHTTPClient.doPost("/units").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(gramm))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("gramm")).andExpect(jsonPath("$.factor").value(gramm.getFactor()));

	}

	@Test
	public void testInvalidCreateEmptyName() throws JacksonException, Exception {
		Unit gramm = new Unit();
		gramm.setName("");
		gramm.setFactor(1.0f / 31.1034768f);
		String body = mockMvc
				.perform(TestHTTPClient.doPost("/units").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(gramm)))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
		
		ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals(400, errorResponse.getStatus());
		assertEquals("Unit name is mandatory.", errorResponse.getMessage());
	}

	@Test
	public void testInvalidCreateNullName() throws JacksonException, Exception {
		Unit gramm = new Unit();
		gramm.setName(null);
		gramm.setFactor(1.0f / 31.1034768f);
		String body = mockMvc
				.perform(TestHTTPClient.doPost("/units").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(gramm)))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
		
		ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals(400, errorResponse.getStatus());
		assertEquals("Unit name is mandatory.", errorResponse.getMessage());
	}
	@Test
	public void testUpdate() throws JacksonException, Exception {
		Unit gramm = new Unit();
		gramm.setName("gramm");
		gramm.setFactor(1.0f);

		Unit created = unitRepository.save(gramm);
		created.setFactor(0.5f);

		mockMvc.perform(TestHTTPClient.doPut("/units/" + created.getName()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(created))).andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("gramm")).andExpect(jsonPath("$.factor").value(0.5));

	}

	@Test
	public void testDelete() throws JacksonException, Exception {
		Unit unit = new Unit();
		unit.setName("OZ");
		unit.setFactor(1);
		unitRepository.save(unit);

		mockMvc.perform(TestHTTPClient.doDelete("/units/" + unit.getName())).andExpect(status().isNoContent());

		assertFalse(unitRepository.existsById(unit.getName()));

		mockMvc.perform(TestHTTPClient.doDelete("/units/" + unit.getName())).andExpect(status().isNotFound());

	}

}
