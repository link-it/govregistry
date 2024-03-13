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
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.transaction.Transactional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import it.govhub.govregistry.api.Application;
import it.govhub.govregistry.api.repository.OrganizationRepository;
import it.govhub.govregistry.api.repository.ServiceRepository;
import it.govhub.govregistry.api.repository.UserRepository;
import it.govhub.govregistry.api.test.Costanti;
import it.govhub.govregistry.api.test.utils.Matchers;
import it.govhub.govregistry.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.api.test.utils.Utils;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.RoleEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.repository.ReadRoleRepository;
import it.govhub.security.cache.Caches;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di creazione delle Authorization")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Authorization_UC_2_CreateAuthorizationFailsTest {
	
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
	void UC_2_01_CreateAuthorizationFail_MissingRole() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_2_02_CreateAuthorizationFail_UserNotFound() throws Exception {
		configurazioneDB();
		int idUser1 = 10000;
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, idUser1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.title", is("Not Found")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_2_03_CreateAuthorizationFail_RoleNotFound() throws Exception {
		configurazioneDB();
		int idUser1 = 10000;
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", idUser1)
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_2_04_CreateAuthorizationFail_OrganizationNotFound() throws Exception {
		configurazioneDB();
		int idUser1 = 10000;
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(idUser1))
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_2_05_CreateAuthorizationFail_ServiceNotFound() throws Exception {
		configurazioneDB();
		int idUser1 = 10000;
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(idUser1))
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_2_06_CreateAuthorizationFail_Unauthorized() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
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
	
	@Test
	void UC_2_07_CreateAuthorizationFail_ExpirationDate_WrongFormat() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		DateTimeFormatter dt = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault());
				
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();
	}
	
	@Test
	void UC_2_08_CreateAuthorizationFail_UserEditor() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB("user_viewer");
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_ruolo_non_assegnabile");
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaUserEditor())
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
	
	@Test
	@Transactional
	void UC_2_09_CreateAuthorizationFail_UserEditor_Organization() throws Exception {
		configurazioneDB();
		// Assegno all'utenza SNakamoto la possibilita' di editare i ruoli
		OrganizationEntity enteCreditore3 = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		UserEntity userSNakamoto = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);

		try {
			deleteAllAuthorizations(userSNakamoto);

			MvcResult result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, userSNakamoto.getId())
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.with(csrf())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andReturn();

//			this.log.info("Leggo la lista delle autorizzazioni");
			JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
			JsonObject userList = reader.readObject();

			// Controlli sulla paginazione
			JsonObject page = userList.getJsonObject("page");
			assertEquals(0, page.getInt("offset"));
			assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
			assertEquals(0, page.getInt("total"));

			RoleEntity ruoloUsersEditor = leggiRuoloDB("govhub_users_editor");

			//		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
			String json = Json.createObjectBuilder()
					.add("role", ruoloUsersEditor.getId())
					.add("organizations", Json.createArrayBuilder().add(enteCreditore3.getId()))
					.add("services", Json.createArrayBuilder())
					//				.add("expiration_date", dt.format(now))
					.build()
					.toString();

//			this.log.info("Assegno il ruolo user_editor a snakamoto sull'organizzazione");
			this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, userSNakamoto.getId())
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.with(csrf())
					.content(json)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.role.role_name", is("govhub_users_editor")))
			.andExpect(jsonPath("$.organizations[0].tax_code", is(enteCreditore3.getTaxCode())))
			.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
			//				.andExpect(jsonPath("$.expiration_date", is(now)))
			.andReturn();

//			this.log.info("Controllo che l'autorizzazione sia stata assegnata");
			result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, userSNakamoto.getId())
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.with(csrf())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andReturn();

			// Leggo la lista delle autorizzazioni
			reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
			userList = reader.readObject();

			// Controlli sulla paginazione
			page = userList.getJsonObject("page");
			assertEquals(0, page.getInt("offset"));
			assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
			assertEquals(1, page.getInt("total"));

			UserEntity userViewer = leggiUtenteDB("user_viewer");

			RoleEntity ruoloGovhubUser = leggiRuoloDB("govhub_user");

			json = Json.createObjectBuilder()
					.add("role", ruoloGovhubUser.getId())
					.add("organizations", Json.createArrayBuilder())
					.add("services", Json.createArrayBuilder())
					//				.add("expiration_date", dt.format(now))
					.build()
					.toString();
			
			evictCaches();

//			this.log.info("Con l'utenza snakamoto autorizzo lo user_viewer al ruolo di govhub_user");
			this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, userViewer.getId())
					.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
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

		} finally {
//			this.log.info("Deleting all Authorizations");
			deleteAllAuthorizations(userSNakamoto);
		}
	}
	
	// Da chiamare per pulire la cache.
	@CacheEvict(value = Caches.PRINCIPALS, allEntries = true)
	private void evictCaches() {
		//this.log.info("Evicting Principal Caches..");
	}

	@Test
	@Transactional
	void UC_2_10_CreateAuthorizationFail_UserEditor_Service() throws Exception {
		configurazioneDB();
		// Assegno all'utenza SNakamoto la possibilita' di editare i ruoli
		ServiceEntity servizioTest = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		UserEntity userSNakamoto = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		try {
			deleteAllAuthorizations(userSNakamoto);

			MvcResult result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, userSNakamoto.getId())
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.with(csrf())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andReturn();

			// Leggo la lista delle autorizzazioni
			JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
			JsonObject userList = reader.readObject();

			// Controlli sulla paginazione
			JsonObject page = userList.getJsonObject("page");
			assertEquals(0, page.getInt("offset"));
			assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
			assertEquals(0, page.getInt("total"));

			RoleEntity ruoloUsersEditor = leggiRuoloDB("govhub_users_editor");

			//		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
			String json = Json.createObjectBuilder()
					.add("role", ruoloUsersEditor.getId())
					.add("organizations", Json.createArrayBuilder())
					.add("services", Json.createArrayBuilder().add(servizioTest.getId()))
					//				.add("expiration_date", dt.format(now))
					.build()
					.toString();

			// Creo una organization e verifico la risposta
			this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, userSNakamoto.getId())
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.with(csrf())
					.content(json)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.role.role_name", is("govhub_users_editor")))
			.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
			.andExpect(jsonPath("$.services[0].service_name", is(servizioTest.getName())))
			//				.andExpect(jsonPath("$.expiration_date", is(now)))
			.andReturn();

			result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, userSNakamoto.getId())
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.with(csrf())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andReturn();

			// Leggo la lista delle autorizzazioni
			reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
			userList = reader.readObject();

			// Controlli sulla paginazione
			page = userList.getJsonObject("page");
			assertEquals(0, page.getInt("offset"));
			assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
			assertEquals(1, page.getInt("total"));

			UserEntity userViewer = leggiUtenteDB("user_viewer");

			RoleEntity ruoloGovhubUser = leggiRuoloDB("govhub_user");

			json = Json.createObjectBuilder()
					.add("role", ruoloGovhubUser.getId())
					.add("organizations", Json.createArrayBuilder())
					.add("services", Json.createArrayBuilder())
					//				.add("expiration_date", dt.format(now))
					.build()
					.toString();

			// Creo una organization e verifico la risposta
			this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, userViewer.getId())
					.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
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
		} finally {
			deleteAllAuthorizations(userSNakamoto);
		}
	}

	@Test
	@Transactional
	void UC_2_11_CreateAuthorizationFail_UserEditor_ExpirationDate() throws Exception {
		configurazioneDB();
		// Assegno all'utenza SNakamoto la possibilita' di editare i ruoli
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		UserEntity userSNakamoto = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);

		try {
			deleteAllAuthorizations(userSNakamoto);

			MvcResult result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, userSNakamoto.getId())
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.with(csrf())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andReturn();

			// Leggo la lista delle autorizzazioni
			JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
			JsonObject userList = reader.readObject();

			// Controlli sulla paginazione
			JsonObject page = userList.getJsonObject("page");
			assertEquals(0, page.getInt("offset"));
			assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
			assertEquals(0, page.getInt("total"));


			RoleEntity ruoloUsersEditor = leggiRuoloDB("govhub_users_editor");

			OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
			String json = Json.createObjectBuilder()
					.add("role", ruoloUsersEditor.getId())
					.add("organizations", Json.createArrayBuilder())
					.add("services", Json.createArrayBuilder().add(servizio.getId()))
					.add("expiration_date", dt.format(now))
					.build()
					.toString();

			// Creo una organization e verifico la risposta
			this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, userSNakamoto.getId())
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.with(csrf())
					.content(json)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.role.role_name", is("govhub_users_editor")))
			.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
			.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
			.andExpect(jsonPath("$.expiration_date", is(dt.format(now))))
			.andReturn();

			result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, userSNakamoto.getId())
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.with(csrf())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andReturn();

			// Leggo la lista delle autorizzazioni
			reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
			userList = reader.readObject();

			// Controlli sulla paginazione
			page = userList.getJsonObject("page");
			assertEquals(0, page.getInt("offset"));
			assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
			assertEquals(1, page.getInt("total"));

			UserEntity userViewer = leggiUtenteDB("user_viewer");

			RoleEntity ruoloGovhubUser = leggiRuoloDB("govhub_user");

			json = Json.createObjectBuilder()
					.add("role", ruoloGovhubUser.getId())
					.add("organizations", Json.createArrayBuilder())
					.add("services", Json.createArrayBuilder())
					//				.add("expiration_date", dt.format(now))
					.build()
					.toString();

			// Creo una organization e verifico la risposta
			this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, userViewer.getId())
					.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
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
		} finally {
			deleteAllAuthorizations(userSNakamoto);
		}
	}
	
	private void deleteAllAuthorizations(UserEntity user) throws Exception {
		Utils.deleteAllAuthorizations(user, this.mockMvc, this.userAuthProfilesUtils.utenzaAdmin());
	}
}

