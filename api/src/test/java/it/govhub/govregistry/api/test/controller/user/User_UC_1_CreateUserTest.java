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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonReader;

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
@DisplayName("Test di censimento Utenti")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

class User_UC_1_CreateUserTest {

	private static final String USERS_BASE_PATH = "/v1/users";

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Test
	void UC_1_01_CreateUserOk() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();

		String json = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.build()
				.toString();

		// Creo un utente e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.enabled", is(user.getEnabled())))
				.andExpect(jsonPath("$.full_name", is(user.getFullName())))
				.andExpect(jsonPath("$.principal", is(user.getPrincipal())))
				.andReturn();
		
		// Leggo l'utente dal servizio e verifico
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		UserEntity userEntity = this.userRepository.findById((long) id).get();
		
		assertEquals(id, userEntity.getId());
		assertEquals(user.getEnabled(), userEntity.getEnabled());
		assertEquals(user.getFullName(), userEntity.getFullName());
		assertEquals(user.getPrincipal(), userEntity.getPrincipal());
		assertNull(userEntity.getEmail());
		
	}
	
	@Test
	void UC_1_02_CreateUserOk_withEmail() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();
		
		String json = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.add("email", user.getEmail())
				.build()
				.toString();

		// Creo un utente e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.email", is(user.getEmail())))
				.andExpect(jsonPath("$.enabled", is(user.getEnabled())))
				.andExpect(jsonPath("$.full_name", is(user.getFullName())))
				.andExpect(jsonPath("$.principal", is(user.getPrincipal())))
				.andReturn();
		
		// Leggo l'utente dal servizio e verifico
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		UserEntity userEntity = this.userRepository.findById((long) id).get();
		
		assertEquals(id, userEntity.getId());
		assertEquals(user.getEnabled(), userEntity.getEnabled());
		assertEquals(user.getFullName(), userEntity.getFullName());
		assertEquals(user.getPrincipal(), userEntity.getPrincipal());
		assertEquals(user.getEmail(), userEntity.getEmail());
		
	}
	
	@Test
	void UC_1_03_CreateUserOk_Enabled() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();
		user.setEnabled(true);

		String json = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.build()
				.toString();

		// Creo un utente e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.enabled", is(user.getEnabled())))
				.andExpect(jsonPath("$.full_name", is(user.getFullName())))
				.andExpect(jsonPath("$.principal", is(user.getPrincipal())))
				.andReturn();
		
		// Leggo l'utente dal servizio e verifico
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		UserEntity userEntity = this.userRepository.findById((long) id).get();
		
		assertEquals(id, userEntity.getId());
		assertEquals(user.getEnabled(), userEntity.getEnabled());
		assertEquals(user.getFullName(), userEntity.getFullName());
		assertEquals(user.getPrincipal(), userEntity.getPrincipal());
		assertNull(userEntity.getEmail());
		
	}
}
