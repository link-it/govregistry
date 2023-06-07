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
package it.govhub.govregistry.api.test.controller.service;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

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
import it.govhub.govregistry.api.repository.ServiceRepository;
import it.govhub.govregistry.api.repository.UserRepository;
import it.govhub.govregistry.api.test.Costanti;
import it.govhub.govregistry.api.test.utils.Utils;
import it.govhub.govregistry.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.api.beans.PatchOp.OpEnum;
import it.govhub.govregistry.commons.entity.RoleEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.repository.ReadRoleRepository;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di controllo autorizzazioni necessarie all'esecuzione delle operazioni sui Servizi")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Service_UC_6_AutorizzazioneUtenzeTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ServiceRepository serviceRepository;
	
	@Autowired
	public ReadRoleRepository roleRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	private void configurazioneDB() {
		UserEntity user = Costanti.getUser_Snakamoto();
		if(leggiUtenteDB(user.getPrincipal()) == null) {
			this.userRepository.save(user);
		}

		ServiceEntity servizio = Costanti.getServizioTest();
		if(leggiServizioDB(servizio.getName()) == null) {
			this.serviceRepository.save(servizio);
		}
		
		ServiceEntity servizio2 = Costanti.getServizioTest2();
		if(leggiServizioDB(servizio2.getName()) == null) {
			this.serviceRepository.save(servizio2);
		}
	}
	
	private RoleEntity leggiRuoloDB(String nomeRuolo) {
		return Utils.leggiRuoloDB(nomeRuolo, this.roleRepository);
	}
	
	private UserEntity leggiUtenteDB(String principal) {
		return Utils.leggiUtenteDB(principal, this.userRepository);
	}
	
	private ServiceEntity leggiServizioDB(String nome) {
		return Utils.leggiServizioDB(nome, this.serviceRepository);
	}
	
	//1. CreateService con utenza non admin con ruolo govhub_services_editor: OK
	@Test
	void UC_6_01_CreateServiceOk_UtenzaConRuolo_GovHub_Services_Editor() throws Exception {
		ServiceEntity servizio = Costanti.getServizioTest2();
		servizio.setName(servizio.getName() + "-601");

		String json = Json.createObjectBuilder()
				.add("service_name", servizio.getName())
				.add("description", servizio.getDescription())
				.build()
				.toString();

		// Creo un service e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(Costanti.SERVICES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaServiceEditor())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.service_name", is(servizio.getName())))
				.andExpect(jsonPath("$.description", is(servizio.getDescription())))
				.andReturn();
		
		// Leggo l'organization dal servizio e verifico
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		ServiceEntity serviceEntity = this.serviceRepository.findById((long) id).get();
		
		assertEquals(id, serviceEntity.getId());
		assertEquals(servizio.getName(), serviceEntity.getName());
		assertEquals(servizio.getDescription(), serviceEntity.getDescription());
		
	}
	
	//2. CreateService con utenza non admin con ruolo non govhub_services_editor: NotAuthorized
	@Test
	void UC_6_02_CreateServiceFail_UtenzaSenzaRuolo_GovHub_Services_Editor() throws Exception {
		ServiceEntity servizio = Costanti.getServizioTest();

		String json = Json.createObjectBuilder()
				.add("service_name", servizio.getName())
				.add("description", servizio.getDescription())
				.build()
				.toString();

		// Creo un service e verifico la risposta
		this.mockMvc.perform(post(Costanti.SERVICES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaServiceViewer())
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
	
	//3. PatchService con utenza non admin con ruolo govhub_services_editor: OK
	@Test
	void UC_6_03_PatchServiceOk_UtenzaConRuolo_GovHub_Services_Editor() throws Exception {
		configurazioneDB();
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST_2);
		long id = servizio.getId();

		// Modifico il servizio
		servizio.setName("SSSSServizio");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/service_name")
				.add("value", servizio.getName());

		String PatchService = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(Costanti.SERVICES_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaServiceEditor())
				.with(csrf())
				.content(PatchService)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.service_name", is(servizio.getName())))
		.andExpect(jsonPath("$.description", is(servizio.getDescription())))
		.andReturn();

		ServiceEntity serviceEntity = this.serviceRepository.findById((long) id).get();
		
		assertEquals(id, serviceEntity.getId());
		assertEquals(servizio.getName(), serviceEntity.getName());
		assertEquals(servizio.getDescription(), serviceEntity.getDescription());
	}
	
	//4. PatchService con utenza non admin con ruolo non govhub_services_editor: NotAuthorized
	@Test
	void UC_6_04_PatchServiceFail_UtenzaSenzaRuolo_GovHub_Services_Editor() throws Exception {
		configurazioneDB();
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST_2);
		long id = servizio.getId();

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REMOVE.toString())
				.add("path", "/service_name")
				.add("value", "");

		String PatchService = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(Costanti.SERVICES_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaServiceViewer())
				.with(csrf())
				.content(PatchService)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();

	}
	
	//5. FindAllServices con utenza non admin con ruolo govhub_services_editor/govhub_services_viewer: OK
	@Test
	void UC_6_05_FindAllOk_UtenzaConRuolo_GovHub_Services_Editor_O_Viewer() throws Exception {
		this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaServiceEditor())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaServiceViewer())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
	}
	
	//6. FindAllServices con utenza non admin con ruolo non govhub_services_editor/govhub_services_viewer: NotAuthorized
	@Test
	void UC_6_06_FindAllFail_UtenzaSenzaRuolo_GovHub_Services_Editor_O_Viewer() throws Exception {
		MvcResult result = this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaOspite())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject serviceList = reader.readObject();
		
		// Controlli sugli items
		JsonArray items = serviceList.getJsonArray("items");
		assertEquals(0, items.size());
	}
	
	//7. GetService con utenza non admin con ruolo govhub_services_editor/govhub_services_viewer: OK
	@Test
	void UC_6_07_GetUserOk_UtenzaConRuolo_GovHub_Services_Editor_O_Viewer() throws Exception {
		MvcResult result = this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		
		JsonObject item1 = items.getJsonObject(0); 
		int idService1 = item1.getInt("id");
		
		this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH_DETAIL_ID,idService1)
				.with(this.userAuthProfilesUtils.utenzaServiceEditor())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH_DETAIL_ID,idService1)
				.with(this.userAuthProfilesUtils.utenzaServiceViewer())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
	}
	
	//8. GetService con utenza non admin con ruolo non govhub_services_editor/govhub_services_viewer: NotAuthorized
	@Test
	void UC_6_08_GetUserFail_UtenzaSenzaRuolo_GovHub_Services_Editor_O_Viewer() throws Exception {
		MvcResult result = this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		
		JsonObject item1 = items.getJsonObject(0); 
		int idService1 = item1.getInt("id");
		
		this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH_DETAIL_ID,idService1)
				.with(this.userAuthProfilesUtils.utenzaOspite())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	//9. FindAllServices con utenza non admin con ruolo govhub_services_editor/govhub_services_viewer: OK che ha autorizzazione solo su un service
	@Test
	void UC_6_09_FindAllOk_UtenzaConRuolo_GovHub_Services_Editor_O_Viewer_Authorization() throws Exception {
		configurazioneDB();
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		RoleEntity ruoloUser = leggiRuoloDB("govhub_services_viewer");
		
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_services_viewer")))
				.andExpect(jsonPath("$.organizations", is(new ArrayList<>())))
				.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
				.andReturn();
		
		result = this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(1, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		assertEquals(servizio.getName(), items.getJsonObject(0).getString("service_name"));
		
	}
	
	//10. GetService con utenza non admin con ruolo govhub_services_editor/govhub_services_viewer: OK che ha autorizzazione solo su un service
	@Test
	void UC_6_10_GetUserOk_UtenzaConRuolo_GovHub_Services_Editor_O_Viewer_Authorization() throws Exception {
		configurazioneDB();
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		RoleEntity ruoloUser = leggiRuoloDB("govhub_services_viewer");
		
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_services_viewer")))
				.andExpect(jsonPath("$.organizations", is(new ArrayList<>())))
				.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
				.andReturn();
		
		result = this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		
		JsonObject item1 = items.getJsonObject(items.size() - 1);  // primo servizio creato non Servizio3
		int idEnte1 = item1.getInt("id"); // Servizio1
				
		// l'utenza non vede altri services a parte quello che gli e' stata assegnata (Servizio 3)
		this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH_DETAIL_ID,idEnte1)
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
		// l'utenza puo' vedere solo l'organization che gli e' stata assegnata (Ente 4)
		this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH_DETAIL_ID, servizio.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
	}
	
	//11. PatchService con utenza non admin con ruolo govhub_services_editor: OK che ha autorizzazione solo su un service
	// @Test
	void UC_6_11_PatchServiceOk_UtenzaConRuolo_GovHub_Services_Editor_Authorization() throws Exception {
		configurazioneDB();
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		RoleEntity ruoloUser = leggiRuoloDB("govhub_services_editor");
		
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_services_editor")))
				.andExpect(jsonPath("$.organizations", is(new ArrayList<>())))
				.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
				.andReturn();
		
		result = this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		
		JsonObject item1 = items.getJsonObject(items.size() - 1);  // primo servizio creato non Servizio3
		int idEnte1 = item1.getInt("id"); // Servizio1
		
		// Modifico il servizio
		servizio.setName("SSSSServizio");
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/service_name")
				.add("value", servizio.getName());
		
		String PatchService = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();
		
		this.mockMvc.perform(patch(Costanti.SERVICES_BASE_PATH_DETAIL_ID, servizio.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.content(PatchService)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.service_name", is(servizio.getName())))
		.andExpect(jsonPath("$.description", is(servizio.getDescription())))
		.andReturn();

		ServiceEntity serviceEntity = this.serviceRepository.findById((long) servizio.getId()).get();
		
		assertEquals(servizio.getId(), serviceEntity.getId());
		assertEquals(servizio.getName(), serviceEntity.getName());
		assertEquals(servizio.getDescription(), serviceEntity.getDescription());
		
		// Patch su un service per cui non si e' autorizzati fallisce
		this.mockMvc.perform(patch(Costanti.SERVICES_BASE_PATH_DETAIL_ID, idEnte1)
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.content(PatchService)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
}

