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
package com.my.goldmanager.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.goldmanager.entity.ItemStorage;
import com.my.goldmanager.repository.ItemStorageRepository;
import com.my.goldmanager.rest.response.ErrorResponse;
import com.my.goldmanager.service.AuthenticationService;
import com.my.goldmanager.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ItemStorageControllerSpringBootTest {

	static class CreateItemStorageTestParameter {
		private String name;
		private String description;
		private boolean successExpected;
		private String expectedErrorMessage;

		public CreateItemStorageTestParameter(String name, String description, boolean successExpected,
				String expectedErrorMessage) {

			this.name = name;
			this.description = description;
			this.successExpected = successExpected;
			this.expectedErrorMessage = expectedErrorMessage;
		}

		@Override
		public String toString() {
			return "CreateItemStorageTestParameter [name=" + name + ", description=" + description
					+ ", successExpected=" + successExpected + "]";
		}
	}

	static class UpdateItemStorageTestParameter {
		private String origName;
		private String origDescription;
		private String name;
		private String description;
		private boolean successExpected;
		private String expectedErrorMessage;

		public UpdateItemStorageTestParameter(String origName, String origDescription, String name, String description,
				boolean successExpected, String expectedErrorMessage) {
			super();
			this.origName = origName;
			this.origDescription = origDescription;
			this.name = name;
			this.description = description;
			this.successExpected = successExpected;
			this.expectedErrorMessage = expectedErrorMessage;
		}

		@Override
		public String toString() {
			return "UpdateItemStorageTestParameter [origName=" + origName + ", origDescription=" + origDescription
					+ ", name=" + name + ", description=" + description + ", successExpected=" + successExpected + "]";
		}

	}

	@Autowired
	private UserService userService;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private ItemStorageRepository itemStorageRepository;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	public void setUp() {
		TestHTTPClient.setup(userService, authenticationService);
	}

	@AfterEach
	public void cleanup() {
		itemStorageRepository.deleteAll();
		TestHTTPClient.cleanup();
	}

	@Test
	void testCreateNameAlreadyExisting() throws JsonProcessingException, UnsupportedEncodingException, Exception {
		ItemStorage itemStorage = new ItemStorage();
		itemStorage.setName("MyName");
		itemStorageRepository.save(itemStorage);
		itemStorage.setDescription("My descriptiom");
		String content = mockMvc
				.perform(TestHTTPClient.doPost("/api/itemStorages").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(itemStorage)))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
		ErrorResponse errorResponse = objectMapper.readValue(content, ErrorResponse.class);
		assertEquals("Item Storage name must be unique.", errorResponse.getMessage());
		assertEquals(400, errorResponse.getStatus());

	}

	@ParameterizedTest
	@MethodSource("createItemStorageTestParameter")
	void testCreate(CreateItemStorageTestParameter param)
			throws JsonProcessingException, UnsupportedEncodingException, Exception {
		ItemStorage itemStorage = new ItemStorage();
		itemStorage.setName(param.name);
		itemStorage.setDescription(param.description);
		if (param.successExpected) {
			String content = mockMvc
					.perform(TestHTTPClient.doPost("/api/itemStorages").contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(itemStorage)))
					.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
			ItemStorage result = objectMapper.readValue(content, ItemStorage.class);
			assertNotNull(result);
			assertNotNull(result.getId());
			assertTrue(itemStorageRepository.existsById(result.getId()));
			assertEquals(param.name.trim(), result.getName());
			assertEquals(param.description, result.getDescription());

		} else {
			String content = mockMvc
					.perform(TestHTTPClient.doPost("/api/itemStorages").contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(itemStorage)))
					.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();

			ErrorResponse errorResponse = objectMapper.readValue(content, ErrorResponse.class);
			assertEquals(param.expectedErrorMessage, errorResponse.getMessage());
			assertEquals(400, errorResponse.getStatus());
		}
	}

	@Test
	void testUpdateNotExisting() throws JsonProcessingException, Exception {
		ItemStorage itemStorage = new ItemStorage();
		itemStorage.setName("MyStorage");
		itemStorage.setDescription("MyDesc");
		itemStorage.setId("invalidId");
		mockMvc.perform(TestHTTPClient.doPut("/api/itemStorages/" + itemStorage.getId())
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(itemStorage)))
				.andExpect(status().isNotFound());
	}

	@Test
	void testGetNotExisting() throws JsonProcessingException, Exception {

		mockMvc.perform(TestHTTPClient.doGet("/api/itemStorages/myId0")).andExpect(status().isNotFound());
	}

	@Test
	void testDelete() throws Exception {
		ItemStorage itemStorage = new ItemStorage();
		itemStorage.setDescription("MyDesc");
		itemStorage.setName("MyName");
		itemStorage = itemStorageRepository.save(itemStorage);
		mockMvc.perform(TestHTTPClient.doDelete("/api/itemStorages/" + itemStorage.getId()))
				.andExpect(status().isNoContent());
		assertFalse(itemStorageRepository.existsById(itemStorage.getId()));
	}

	@Test
	void testDeleteNotFound() throws Exception {

		mockMvc.perform(TestHTTPClient.doDelete("/api/itemStorages/myid0")).andExpect(status().isNotFound());
	}

	@Test
	void testListAll() throws UnsupportedEncodingException, Exception {
		
		for(int i= 0; i <100; i++) {
			ItemStorage itemStorage = new ItemStorage();
			itemStorage.setName("MyStorage "+i);
			if(i%3 == 0) {
				itemStorage.setDescription(null);
			}
			else if(i%7 == 0) {
				itemStorage.setDescription("");
			}
			else {
				itemStorage.setDescription("My Description "+i);
			}
			itemStorageRepository.save(itemStorage);
		}
		
		List<ItemStorage> expectedStorages = itemStorageRepository.findAll();

		String body = mockMvc.perform(TestHTTPClient.doGet("/api/itemStorages")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse()
				.getContentAsString();

		JsonNode node = objectMapper.readTree(body);
		assertFalse(node.isEmpty());
		assertEquals(expectedStorages.size(), node.size());
		for (int current = 0; current < expectedStorages.size(); current++) {
			ItemStorage expected = expectedStorages.get(current);

			ItemStorage result = objectMapper.readValue(node.get(current).toString(), ItemStorage.class);
			assertEquals(expected.getId(), result.getId());
			assertEquals(expected.getName(), result.getName());
			assertEquals(expected.getDescription(), result.getDescription());
		}
	}
	@Test
	void testGet() throws JsonProcessingException, Exception {

		ItemStorage expected = new ItemStorage();
		expected.setDescription("MyDesc");
		expected.setName("MyName");
		expected = itemStorageRepository.save(expected);

		String content = mockMvc.perform(TestHTTPClient.doGet("/api/itemStorages/" + expected.getId()))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		ItemStorage result = objectMapper.readValue(content, ItemStorage.class);
		assertEquals(expected.getId(), result.getId());
		assertEquals(expected.getName(), result.getName());
		assertEquals(expected.getDescription(), result.getDescription());

	}

	@ParameterizedTest
	@MethodSource("updateItemstorageTestParameter")
	void testUpdate(UpdateItemStorageTestParameter param)
			throws JsonProcessingException, UnsupportedEncodingException, Exception {
		ItemStorage original = new ItemStorage();
		original.setName(param.origName);
		original.setDescription(param.origDescription);
		ItemStorage itemStorage = itemStorageRepository.save(original);

		itemStorage.setName(param.name);
		itemStorage.setDescription(param.description);
		if (param.successExpected) {
			String content = mockMvc
					.perform(TestHTTPClient.doPut("/api/itemStorages/" + itemStorage.getId())
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(itemStorage)))
					.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
			ItemStorage result = objectMapper.readValue(content, ItemStorage.class);
			assertNotNull(result);
			assertNotNull(result.getId());
			assertEquals(itemStorage.getId(), result.getId());
			assertEquals(itemStorage.getName().trim(), result.getName());
			assertEquals(itemStorage.getDescription(), result.getDescription());

		} else {
			String content = mockMvc
					.perform(TestHTTPClient.doPut("/api/itemStorages/" + itemStorage.getId())
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(itemStorage)))
					.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();

			ErrorResponse errorResponse = objectMapper.readValue(content, ErrorResponse.class);
			assertEquals(param.expectedErrorMessage, errorResponse.getMessage());
			assertEquals(400, errorResponse.getStatus());
		}
	}

	static Stream<UpdateItemStorageTestParameter> updateItemstorageTestParameter() {
		return Stream.of(
				new UpdateItemStorageTestParameter("My Storage", "My Description", null, null, false,
						"Item Storage name is mandatory."),
				new UpdateItemStorageTestParameter("My Storage", "My Description", " ", null, false,
						"Item Storage name is mandatory."),
				new UpdateItemStorageTestParameter("My Storage", "My Description", "My Storage n1 ", null, true, null),
				new UpdateItemStorageTestParameter("My Storage", "My Description", "My Storage n2", " ", true, null),
				new UpdateItemStorageTestParameter("My Storage", "My Description", "My Storage n2",
						"My new description", true, null),
				new UpdateItemStorageTestParameter("My Storage", null, "My Storage n2", "My new description", true,
						null));
	}

	static Stream<CreateItemStorageTestParameter> createItemStorageTestParameter() {

		return Stream.of(new CreateItemStorageTestParameter(null, null, false, "Item Storage name is mandatory."),
				new CreateItemStorageTestParameter("", null, false, "Item Storage name is mandatory."),
				new CreateItemStorageTestParameter(" ", null, false, "Item Storage name is mandatory."),
				new CreateItemStorageTestParameter("My Storage", null, true, null),
				new CreateItemStorageTestParameter("My Storage", "", true, null),
				new CreateItemStorageTestParameter("My Storage", "My description", true, null),
				new CreateItemStorageTestParameter("My Storage ", "My description", true, null));
	}

}
