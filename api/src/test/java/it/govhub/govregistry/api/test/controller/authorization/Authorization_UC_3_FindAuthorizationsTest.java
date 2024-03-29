/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
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
package it.govhub.govregistry.api.test.controller.authorization;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import it.govhub.govregistry.api.Application;
import it.govhub.govregistry.api.repository.OrganizationRepository;
import it.govhub.govregistry.api.repository.ServiceRepository;
import it.govhub.govregistry.api.repository.UserRepository;
import it.govhub.govregistry.api.test.Costanti;
import it.govhub.govregistry.api.test.utils.Matchers;
import it.govhub.govregistry.api.test.utils.Utils;
import it.govhub.govregistry.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.RoleEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.repository.ReadRoleRepository;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di ricerca delle Authorization")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
class Authorization_UC_3_FindAuthorizationsTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Autowired
	private ServiceRepository serviceRepository;
	
	@Autowired
	public ReadRoleRepository roleRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Autowired
	private UserRepository userRepository;
	
	private DateTimeFormatter dt = DateTimeFormatter.ISO_DATE_TIME;
	
	@Value("${govhub.time-zone:Europe/Rome}")
	private String timeZone;
	
	@BeforeEach
	private void configurazioneDB() {
		UserEntity user = Costanti.getUser_Snakamoto();
		if(leggiUtenteDB(user.getPrincipal()) == null) {
			this.userRepository.save(user);
		}
		
		OrganizationEntity ente2 = Costanti.getEnteCreditore3();
		if(leggiEnteDB(ente2.getTaxCode()) == null) {
			this.organizationRepository.save(ente2);
		}

		ServiceEntity servizio = Costanti.getServizioTest();
		if(leggiServizioDB(servizio.getName()) == null) {
			this.serviceRepository.save(servizio);
		}
	}
	
	private RoleEntity leggiRuoloDB(String nomeRuolo) {
		return Utils.leggiRuoloDB(nomeRuolo, this.roleRepository);
	}
	
	private UserEntity leggiUtenteDB(String principal) {
		return Utils.leggiUtenteDB(principal, this.userRepository);
	}
	
	private OrganizationEntity leggiEnteDB(String nome) {
		return Utils.leggiEnteDB(nome, this.organizationRepository);
	}
	
	private ServiceEntity leggiServizioDB(String nome) {
		return Utils.leggiServizioDB(nome, this.serviceRepository);
	}
	
	@Test
	void UC_3_01_FindAuthorizationsOk() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		deleteAllAuthorizations(user);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una authorization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
//				.andExpect(jsonPath("$.expiration_date", is(now)))
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole = reader.readObject().getInt("id");
		
		result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(1, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		assertEquals(idRole, items.getJsonObject(0).getInt("id"));
		assertEquals(ruoloUser.getName(), items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals(0, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(0, items.getJsonObject(0).getJsonArray("services").size());
		
		// Applicazione
		assertEquals(Costanti.APPLICATION_GOVREGISTRY, items.getJsonObject(0).getJsonObject("role").getString("application"));
	}
	
	@Test
	void UC_3_02_FindAuthorizationsOk_Organization() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		deleteAllAuthorizations(user);
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una authorization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect(jsonPath("$.organizations[0].tax_code", is(ente.getTaxCode())))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
//				.andExpect(jsonPath("$.expiration_date", is(now)))
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole = reader.readObject().getInt("id");
		
		result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(1, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		assertEquals(idRole, items.getJsonObject(0).getInt("id"));
		assertEquals(ruoloUser.getName(), items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals(1, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(0, items.getJsonObject(0).getJsonArray("services").size());
		assertEquals(ente.getTaxCode(), items.getJsonObject(0).getJsonArray("organizations").getJsonObject(0).getString("tax_code"));
		
		// Applicazione
		assertEquals(Costanti.APPLICATION_GOVREGISTRY, items.getJsonObject(0).getJsonObject("role").getString("application"));
	}
	
	@Test
	void UC_3_03_FindAuthorizationsOk_Service() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		deleteAllAuthorizations(user);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una authorization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
				.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
//				.andExpect(jsonPath("$.expiration_date", is(now)))
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole = reader.readObject().getInt("id");
		
		result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(1, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		assertEquals(idRole, items.getJsonObject(0).getInt("id"));
		assertEquals(ruoloUser.getName(), items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals(0, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(1, items.getJsonObject(0).getJsonArray("services").size());
		assertEquals(servizio.getName(), items.getJsonObject(0).getJsonArray("services").getJsonObject(0).getString("service_name"));
		// aggiungere check data dopo che si sistema il servizio
		
		// Applicazione
		assertEquals(Costanti.APPLICATION_GOVREGISTRY, items.getJsonObject(0).getJsonObject("role").getString("application"));
	}
	
	@Test
	void UC_3_04_FindAuthorizationsOk_SortIdAsc() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		deleteAllAuthorizations(user);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		RoleEntity ruoloUser2 = leggiRuoloDB("govhub_organizations_viewer");
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una authorization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
//				.andExpect(jsonPath("$.expiration_date", is(now)))
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole1 = reader.readObject().getInt("id");
		
		 json = Json.createObjectBuilder()
					.add("role", ruoloUser2.getId())
					.add("organizations", Json.createArrayBuilder())
					.add("services", Json.createArrayBuilder())
					.add("expiration_date", dt.format(now))
					.build()
					.toString();
		
		 result = this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.with(csrf())
					.content(json)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.id").isNumber())
					.andExpect(jsonPath("$.role.role_name", is("govhub_organizations_viewer")))
					.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
					.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
//					.andExpect(jsonPath("$.expiration_date", is(now)))
					.andReturn();
		 
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole2 = reader.readObject().getInt("id");
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "id");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_ASC);
		
		result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId()).params(params)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());
		
		JsonObject item0 = items.getJsonObject(0);
		assertEquals(idRole1, item0.getInt("id"));
		assertEquals(ruoloUser.getName(), item0.getJsonObject("role").getString("role_name"));
		assertEquals(0, item0.getJsonArray("organizations").size());
		assertEquals(0, item0.getJsonArray("services").size());
		
		// Applicazione
		assertEquals(Costanti.APPLICATION_GOVREGISTRY, item0.getJsonObject("role").getString("application"));
		
		JsonObject item1 = items.getJsonObject(1);
		assertEquals(idRole2, item1.getInt("id"));
		assertEquals(ruoloUser2.getName(), item1.getJsonObject("role").getString("role_name"));
		assertEquals(0, item1.getJsonArray("organizations").size());
		assertEquals(0, item1.getJsonArray("services").size());
		
		// Applicazione
		assertEquals(Costanti.APPLICATION_GOVREGISTRY, item1.getJsonObject("role").getString("application"));
	}
	
	@Test
	void UC_3_05_FindAuthorizationsOk_SortRoleNameAsc() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		deleteAllAuthorizations(user);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		RoleEntity ruoloUser2 = leggiRuoloDB("govhub_organizations_viewer");
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una authorization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
//				.andExpect(jsonPath("$.expiration_date", is(now)))
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole1 = reader.readObject().getInt("id");
		
		 json = Json.createObjectBuilder()
					.add("role", ruoloUser2.getId())
					.add("organizations", Json.createArrayBuilder())
					.add("services", Json.createArrayBuilder())
					.add("expiration_date", dt.format(now))
					.build()
					.toString();
		
		 result = this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.with(csrf())
					.content(json)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.id").isNumber())
					.andExpect(jsonPath("$.role.role_name", is("govhub_organizations_viewer")))
					.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
					.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
//					.andExpect(jsonPath("$.expiration_date", is(now)))
					.andReturn();
		 
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole2 = reader.readObject().getInt("id");
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "role.name");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_ASC);
		
		result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId()).params(params)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		// Leggo la lista delle autorizzazioni	
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());
		
		JsonObject item0 = items.getJsonObject(0);
		assertEquals(idRole2, item0.getInt("id"));
		assertEquals(ruoloUser2.getName(), item0.getJsonObject("role").getString("role_name"));
		assertEquals(0, item0.getJsonArray("organizations").size());
		assertEquals(0, item0.getJsonArray("services").size());
		
		// Applicazione
		assertEquals(Costanti.APPLICATION_GOVREGISTRY, item0.getJsonObject("role").getString("application"));
		
		JsonObject item1 = items.getJsonObject(1);
		assertEquals(idRole1, item1.getInt("id"));
		assertEquals(ruoloUser.getName(), item1.getJsonObject("role").getString("role_name"));
		assertEquals(0, item1.getJsonArray("organizations").size());
		assertEquals(0, item1.getJsonArray("services").size());
		
		// Applicazione
		assertEquals(Costanti.APPLICATION_GOVREGISTRY, item1.getJsonObject("role").getString("application"));
		
	}
	
	@Test
	void UC_3_05_FindAuthorizationsOk_Sort_Unsorted() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		deleteAllAuthorizations(user);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		RoleEntity ruoloUser2 = leggiRuoloDB("govhub_organizations_viewer");
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una authorization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
//				.andExpect(jsonPath("$.expiration_date", is(now)))
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole1 = reader.readObject().getInt("id");
		
		 json = Json.createObjectBuilder()
					.add("role", ruoloUser2.getId())
					.add("organizations", Json.createArrayBuilder())
					.add("services", Json.createArrayBuilder())
					.add("expiration_date", dt.format(now))
					.build()
					.toString();
		
		 result = this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.with(csrf())
					.content(json)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.id").isNumber())
					.andExpect(jsonPath("$.role.role_name", is("govhub_organizations_viewer")))
					.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
					.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
//					.andExpect(jsonPath("$.expiration_date", is(now)))
					.andReturn();
		 
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole2 = reader.readObject().getInt("id");
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "unsorted");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_ASC);
		
		result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId()).params(params)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());
		
		JsonObject item0 = items.getJsonObject(0);
		assertEquals(idRole1, item0.getInt("id"));
		assertEquals(ruoloUser.getName(), item0.getJsonObject("role").getString("role_name"));
		assertEquals(0, item0.getJsonArray("organizations").size());
		assertEquals(0, item0.getJsonArray("services").size());
		
		// Applicazione
		assertEquals(Costanti.APPLICATION_GOVREGISTRY, item0.getJsonObject("role").getString("application"));
		
		JsonObject item1 = items.getJsonObject(1);
		assertEquals(idRole2, item1.getInt("id"));
		assertEquals(ruoloUser2.getName(), item1.getJsonObject("role").getString("role_name"));
		assertEquals(0, item1.getJsonArray("organizations").size());
		assertEquals(0, item1.getJsonArray("services").size());
		
		// Applicazione
		assertEquals(Costanti.APPLICATION_GOVREGISTRY, item1.getJsonObject("role").getString("application"));
	}
	
	private void deleteAllAuthorizations(UserEntity user) throws Exception {
		Utils.deleteAllAuthorizations(user, this.mockMvc, this.userAuthProfilesUtils.utenzaAdmin());
	}
}

