/*
 * GovRegistry - Registries manager for GovHub
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govhub.govregistry.api.test.controller.user;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import it.govhub.govregistry.api.Application;
import it.govhub.govregistry.api.repository.UserRepository;
import it.govhub.govregistry.api.test.Costanti;
import it.govhub.govregistry.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.UserEntity;



@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura degli Utenti")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

class User_UC_5_GetUsersTest {

	private static final String USERS_BASE_PATH = "/v1/users";
	private static final String USERS_BASE_PATH_DETAIL_ID = USERS_BASE_PATH + "/{id}";

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@BeforeEach
	private void caricaUtenti() {
		UserEntity user = Costanti.getUser_Snakamoto();
		this.userRepository.save(user);
		
		UserEntity user2 = Costanti.getUser_Vbuterin();
		this.userRepository.save(user2);
	}
	
	@Test
	void UC_5_01_GetUserOk() throws Exception {
		UserEntity user = Costanti.getUser_Vbuterin();
		
		MvcResult result = this.mockMvc.perform(get(USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(10, items.size());
		
		JsonObject item1 = items.getJsonObject(0); 
		int idUser1 = item1.getInt("id");
		
		result = this.mockMvc.perform(get(USERS_BASE_PATH_DETAIL_ID,idUser1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject item = reader.readObject();
		
		assertEquals(user.getEnabled(), Boolean.parseBoolean(item.get("enabled").toString()));
		assertEquals(user.getFullName(), item.getString("full_name"));
		assertEquals(user.getPrincipal(), item.getString("principal"));
		
	}
	
	@Test
	void UC_5_02_GetUser_NotFound() throws Exception {
		int idUser1 = 10000;
		
		this.mockMvc.perform(get(USERS_BASE_PATH_DETAIL_ID,idUser1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.title", is("Not Found")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	@Test	
	void UC_5_03_GetUser_InvalidId() throws Exception {
		String idUser1 = "XXX";
		
		this.mockMvc.perform(get(USERS_BASE_PATH_DETAIL_ID,idUser1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
}

