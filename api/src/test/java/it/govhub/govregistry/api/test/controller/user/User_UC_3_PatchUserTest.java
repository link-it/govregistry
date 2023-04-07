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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import it.govhub.govregistry.commons.api.beans.PatchOp.OpEnum;
import it.govhub.govregistry.commons.entity.UserEntity;



@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di censimento Utenti")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

class User_UC_3_PatchUserTest {

	private static final String USERS_BASE_PATH = "/v1/users";
	private static final String USERS_BASE_PATH_DETAIL_ID = USERS_BASE_PATH + "/{id}";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;

	@Test
	void UC_3_01_PatchUser_WrongMediaType() throws Exception {
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/enabled")
				.add("value", true);

		String patchUser = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(USERS_BASE_PATH_DETAIL_ID, 1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(patchUser)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(400))
				.andExpect(jsonPath("$.detail", is("Content type 'application/json' not supported")))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type", is("https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request")))
				.andReturn();
	}

	@Test
	void UC_3_02_PatchUser_WrongPayload() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();

		String createUser = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.build()
				.toString();

		// Creo un utente
		MvcResult result = this.mockMvc.perform(post(USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(createUser)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andReturn();

		// Creo un messaggio di patch errato, la specifica prevede che venga inviato un'array di operazioni
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		String patchUser = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/enabled")
				.add("value", user.getEnabled())
				.build()
				.toString();

		this.mockMvc.perform(patch(USERS_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(patchUser)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(400))
				.andExpect(jsonPath("$.detail").isString())
				.andExpect(jsonPath("$.type", is("https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request")))
				.andReturn();

		UserEntity userEntity = this.userRepository.findById((long) id).get();

		assertEquals(id, userEntity.getId());
		assertEquals(user.getEnabled(), userEntity.getEnabled());
		assertEquals(user.getFullName(), userEntity.getFullName());
		assertEquals(user.getPrincipal(), userEntity.getPrincipal());
	}

	@Test
	void UC_3_03_PatchUser_Enabled() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();

		String createUser = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.build()
				.toString();

		// Creo un utente
		MvcResult result = this.mockMvc.perform(post(USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(createUser)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andReturn();

		// Modifico l'utente
		user.setEnabled(true);

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/enabled")
				.add("value", user.getEnabled());

		String patchUser = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(USERS_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(patchUser)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.enabled", is(user.getEnabled())))
		.andExpect(jsonPath("$.full_name", is(user.getFullName())))
		.andExpect(jsonPath("$.principal", is(user.getPrincipal())))
		.andReturn();

		UserEntity userEntity = this.userRepository.findById((long) id).get();

		assertEquals(id, userEntity.getId());
		assertEquals(user.getEnabled(), userEntity.getEnabled());
		assertEquals(user.getFullName(), userEntity.getFullName());
		assertEquals(user.getPrincipal(), userEntity.getPrincipal());
	}

	@Test
	void UC_3_04_PatchUser_Fullname() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();

		String createUser = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.build()
				.toString();

		// Creo un utente
		MvcResult result = this.mockMvc.perform(post(USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(createUser)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andReturn();

		// Modifico l'utente
		user.setFullName(Costanti.FULL_NAME_VITALIY_BUTERIN);

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/full_name")
				.add("value", user.getFullName());

		String patchUser = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(USERS_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(patchUser)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.enabled", is(user.getEnabled())))
		.andExpect(jsonPath("$.full_name", is(user.getFullName())))
		.andExpect(jsonPath("$.principal", is(user.getPrincipal())))
		.andReturn();

		UserEntity userEntity = this.userRepository.findById((long) id).get();

		assertEquals(id, userEntity.getId());
		assertEquals(user.getEnabled(), userEntity.getEnabled());
		assertEquals(user.getFullName(), userEntity.getFullName());
		assertEquals(user.getPrincipal(), userEntity.getPrincipal());
	}

	@Test
	void UC_3_05_PatchUser_Principal() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();

		String createUser = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.build()
				.toString();

		// Creo un utente
		MvcResult result = this.mockMvc.perform(post(USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(createUser)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andReturn();

		// Modifico l'utente
		user.setPrincipal(Costanti.PRINCIPAL_VBUTERIN);

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/principal")
				.add("value", user.getPrincipal());

		String patchUser = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(USERS_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(patchUser)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.enabled", is(user.getEnabled())))
		.andExpect(jsonPath("$.full_name", is(user.getFullName())))
		.andExpect(jsonPath("$.principal", is(user.getPrincipal())))
		.andReturn();

		UserEntity userEntity = this.userRepository.findById((long) id).get();

		assertEquals(id, userEntity.getId());
		assertEquals(user.getEnabled(), userEntity.getEnabled());
		assertEquals(user.getFullName(), userEntity.getFullName());
		assertEquals(user.getPrincipal(), userEntity.getPrincipal());
	}

	@Test
	void UC_3_06_PatchUser_ReplaceEmail() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();

		String createUser = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.add("email", user.getEmail())
				.build()
				.toString();

		// Creo un utente
		MvcResult result = this.mockMvc.perform(post(USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(createUser)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andReturn();

		// Modifico l'utente
		user.setEmail(Costanti.EMAIL_VBUTERIN);

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/email")
				.add("value", user.getEmail());

		String patchUser = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(USERS_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(patchUser)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.enabled", is(user.getEnabled())))
		.andExpect(jsonPath("$.full_name", is(user.getFullName())))
		.andExpect(jsonPath("$.principal", is(user.getPrincipal())))
		.andExpect(jsonPath("$.email", is(user.getEmail())))
		.andReturn();

		UserEntity userEntity = this.userRepository.findById((long) id).get();

		assertEquals(id, userEntity.getId());
		assertEquals(user.getEnabled(), userEntity.getEnabled());
		assertEquals(user.getFullName(), userEntity.getFullName());
		assertEquals(user.getPrincipal(), userEntity.getPrincipal());
	}

	@Test
	void UC_3_07_PatchUser_AddEmail() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();

		String createUser = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				//				.add("email", user.getEmail())
				.build()
				.toString();

		// Creo un utente
		MvcResult result = this.mockMvc.perform(post(USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(createUser)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andReturn();

		// Modifico l'utente
		user.setEmail(Costanti.EMAIL_VBUTERIN);

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.ADD.toString())
				.add("path", "/email")
				.add("value", user.getEmail());

		String patchUser = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(USERS_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(patchUser)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.enabled", is(user.getEnabled())))
		.andExpect(jsonPath("$.full_name", is(user.getFullName())))
		.andExpect(jsonPath("$.principal", is(user.getPrincipal())))
		.andExpect(jsonPath("$.email", is(user.getEmail())))
		.andReturn();

		UserEntity userEntity = this.userRepository.findById((long) id).get();

		assertEquals(id, userEntity.getId());
		assertEquals(user.getEnabled(), userEntity.getEnabled());
		assertEquals(user.getFullName(), userEntity.getFullName());
		assertEquals(user.getPrincipal(), userEntity.getPrincipal());
	}

	@Test
	void UC_3_08_PatchUser_RemoveEmail() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();

		String createUser = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.add("email", user.getEmail())
				.build()
				.toString();

		// Creo un utente
		MvcResult result = this.mockMvc.perform(post(USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(createUser)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andReturn();

		// Modifico l'utente
		user.setEmail(Costanti.EMAIL_VBUTERIN);

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REMOVE.toString())
				.add("path", "/email")
				.add("value", "");

		String patchUser = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(USERS_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(patchUser)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.enabled", is(user.getEnabled())))
		.andExpect(jsonPath("$.full_name", is(user.getFullName())))
		.andExpect(jsonPath("$.principal", is(user.getPrincipal())))
		.andReturn();

		UserEntity userEntity = this.userRepository.findById((long) id).get();

		assertEquals(id, userEntity.getId());
		assertEquals(user.getEnabled(), userEntity.getEnabled());
		assertEquals(user.getFullName(), userEntity.getFullName());
		assertEquals(user.getPrincipal(), userEntity.getPrincipal());
	}

	@Test
	void UC_3_09_PatchUser_ReplaceNotExistingEmail() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();

		String createUser = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.build()
				.toString();

		// Creo un utente
		MvcResult result = this.mockMvc.perform(post(USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(createUser)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andReturn();

		// Modifico l'utente
		user.setEmail(Costanti.EMAIL_VBUTERIN);

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/email")
				.add("value", user.getEmail());

		String patchUser = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(USERS_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(patchUser)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();

		UserEntity userEntity = this.userRepository.findById((long) id).get();

		assertEquals(id, userEntity.getId());
		assertEquals(user.getEnabled(), userEntity.getEnabled());
		assertEquals(user.getFullName(), userEntity.getFullName());
		assertEquals(user.getPrincipal(), userEntity.getPrincipal());
		// Email non e' stata cambiata deve essere ancora null
		assertNull(userEntity.getEmail());
	}
	
	@Test
	void UC_3_10_PatchUser_ConflictPrincipal() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();

		String createUser = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.build()
				.toString();

		// Creo un utente
		MvcResult result = this.mockMvc.perform(post(USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(createUser)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andReturn();

		// Modifico l'utente
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/principal")
				.add("value", "amministratore");

		String patchUser = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(USERS_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(patchUser)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isConflict())
		.andExpect(jsonPath("$.status", is(409)))
		.andExpect(jsonPath("$.title", is("Conflict")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();

		UserEntity userEntity = this.userRepository.findById((long) id).get();

		assertEquals(id, userEntity.getId());
		assertEquals(user.getEnabled(), userEntity.getEnabled());
		assertEquals(user.getFullName(), userEntity.getFullName());
		assertEquals(user.getPrincipal(), userEntity.getPrincipal());
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"/enabled","/full_name","/principal"})
	void UC_3_11_PatchUserRemoveRequiredField(String patchField) throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();

		String createUser = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.add("email", user.getEmail())
				.build()
				.toString();

		// Creo un utente
		MvcResult result = this.mockMvc.perform(post(USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(createUser)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andReturn();

		// Modifico l'utente cancellando il principal
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REMOVE.toString())
				.add("path", patchField)
				.add("value", "");

		String patchUser = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(USERS_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(patchUser)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();

		UserEntity userEntity = this.userRepository.findById((long) id).get();

		assertEquals(id, userEntity.getId());
		assertEquals(user.getEnabled(), userEntity.getEnabled());
		assertEquals(user.getFullName(), userEntity.getFullName());
		assertEquals(user.getPrincipal(), userEntity.getPrincipal());
	}
	
	@Test
	void UC_3_12_PatchUser_WrongOperation() throws Exception {
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", "xxx")
				.add("path", "/enabled")
				.add("value", true);

		String patchUser = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(USERS_BASE_PATH_DETAIL_ID, 1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(patchUser)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
}
