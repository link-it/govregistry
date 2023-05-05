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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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
import it.govhub.govregistry.api.test.utils.Utils;
import it.govhub.govregistry.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.api.beans.PatchOp.OpEnum;
import it.govhub.govregistry.commons.entity.UserEntity;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di controllo autorizzazioni necessarie all'esecuzione delle operazioni sugli Utenti")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class User_UC_6_AutorizzazioneUtenzeTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	private void configurazioneDB() {
		UserEntity user = Costanti.getUser_Snakamoto();
		if(leggiUtenteDB(user.getPrincipal()) == null) {
			this.userRepository.save(user);
		}
	}
	
	private UserEntity leggiUtenteDB(String principal) {
		return Utils.leggiUtenteDB(principal, this.userRepository);
	}

	//1. CreateUser con utenza non admin con ruolo govhub_users_editor: OK
	@Test
	void UC_6_01_CreateUserOk_UtenzaConRuolo_GovHub_Users_Editor() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();
		user.setPrincipal(user.getPrincipal() + "-601");

		String json = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.build()
				.toString();

		// Creo un utente e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(Costanti.USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaUserEditor())
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
	
	//2. CreateUser con utenza non admin con ruolo non govhub_users_editor: NotAuthorized
	@Test
	void UC_6_02_CreateUserFail_UtenzaSenzaRuolo_GovHub_Users_Editor() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();
		
		String json = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.build()
				.toString();
		
		// Creazione Utente non autorizzata
		this.mockMvc.perform(post(Costanti.USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaUserViewer())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	//3. PatchUser con utenza non admin con ruolo govhub_users_editor: OK
	@Test
	void UC_6_03_PatchUserOk_UtenzaConRuolo_GovHub_Users_Editor() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);

		// Modifico l'utente
		user.setEnabled(true);

		long id = user.getId();

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/enabled")
				.add("value", user.getEnabled());

		String patchUser = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(Costanti.USERS_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaUserEditor())
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
	
	//4. PatchUser con utenza non admin con ruolo non govhub_users_editor: NotAuthorized
	@Test
	void UC_6_04_PatchUserFail_UtenzaSenzaRuolo_GovHub_Users_Editor() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);

		long id = user.getId();

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/enabled")
				.add("value", user.getEnabled());

		String patchUser = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(Costanti.USERS_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaUserViewer())
				.with(csrf())
				.content(patchUser)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	//5. FindAllUsers con utenza non admin con ruolo govhub_users_editor/govhub_users_viewer: OK
	@Test
	void UC_6_05_FindAllUsersOk_UtenzaConRuolo_GovHub_Users_Editor_O_Viewer() throws Exception {
		this.mockMvc.perform(get(Costanti.USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaUserEditor())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		this.mockMvc.perform(get(Costanti.USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaUserViewer())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
	}
	
	//6. FindAllUsers con utenza non admin con ruolo non govhub_users_editor/govhub_users_viewer: NotAuthorized
	@Test
	void UC_6_06_FindAllUsersFail_UtenzaSenzaRuolo_GovHub_Users_Editor_O_Viewer() throws Exception {
		this.mockMvc.perform(get(Costanti.USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaOspite())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	//7. GetUser con utenza non admin con ruolo govhub_users_editor/govhub_users_viewer: OK
	@Test
	void UC_6_07_GetUserOk_UtenzaConRuolo_GovHub_Users_Editor_O_Viewer() throws Exception {
		MvcResult result = this.mockMvc.perform(get(Costanti.USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		
		JsonObject item1 = items.getJsonObject(0); 
		int idUser1 = item1.getInt("id");
		
		this.mockMvc.perform(get(Costanti.USERS_BASE_PATH_DETAIL_ID,idUser1)
				.with(this.userAuthProfilesUtils.utenzaUserEditor())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		this.mockMvc.perform(get(Costanti.USERS_BASE_PATH_DETAIL_ID,idUser1)
				.with(this.userAuthProfilesUtils.utenzaUserViewer())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
	}
	
	//8. GetUser con utenza non admin con ruolo non govhub_users_editor/govhub_users_viewer: NotAuthorized
	@Test
	void UC_6_08_GetUserOk_UtenzaSenzaRuolo_GovHub_Users_Editor_O_Viewer() throws Exception {
		MvcResult result = this.mockMvc.perform(get(Costanti.USERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		
		JsonObject item1 = items.getJsonObject(0); 
		int idUser1 = item1.getInt("id");
		
		this.mockMvc.perform(get(Costanti.USERS_BASE_PATH_DETAIL_ID,idUser1)
				.with(this.userAuthProfilesUtils.utenzaOspite())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
}
