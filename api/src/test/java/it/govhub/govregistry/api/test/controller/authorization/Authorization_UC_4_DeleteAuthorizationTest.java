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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
@DisplayName("Test di cancellazione delle Authorization")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
class Authorization_UC_4_DeleteAuthorizationTest {
	
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
	void UC_4_01_DeleteAuthorizationOk() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		deleteAllAuthorizations(user);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime expirationDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
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
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
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
		
		// Cancellazione Autorizzazione
		this.mockMvc.perform(delete(Costanti.AUTHORIZATIONS_BASE_PATH_DETAIL_ID, user.getId().intValue(), idRole)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isOk())
				.andReturn();
		
		// Verifica che la lista autorizzazioni sia vuota
		result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
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
		assertEquals(0, page.getInt("total"));
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(0, items.size());
	}
	
	@Test
	void UC_4_02_DeleteAuthorizationOk_Organization() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		deleteAllAuthorizations(user);
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime expirationDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
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
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect(jsonPath("$.organizations[0].tax_code", is(ente.getTaxCode())))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
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
		
		// Cancellazione Autorizzazione
		this.mockMvc.perform(delete(Costanti.AUTHORIZATIONS_BASE_PATH_DETAIL_ID, user.getId().intValue(), idRole)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isOk())
				.andReturn();
		
		// Verifica che la lista autorizzazioni sia vuota
		result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
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
		assertEquals(0, page.getInt("total"));
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(0, items.size());
	}
	
	@Test
	void UC_4_03_DeleteAuthorizationOk_Service() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		deleteAllAuthorizations(user);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime expirationDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
				.add("expiration_date", dt.format(expirationDate))
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
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
				.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
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
		
		// Cancellazione Autorizzazione
		this.mockMvc.perform(delete(Costanti.AUTHORIZATIONS_BASE_PATH_DETAIL_ID, user.getId().intValue(), idRole)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isOk())
				.andReturn();
		
		// Verifica che la lista autorizzazioni sia vuota
		result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
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
		assertEquals(0, page.getInt("total"));
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(0, items.size());
	}
	
	@Test
	void UC_4_04_DeleteAuthorizationOk_UserEditor() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB("user_viewer");
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime expirationDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaUserEditor())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
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
		assertEquals(2, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());
		
		assertEquals(idRole, items.getJsonObject(0).getInt("id"));
		assertEquals(ruoloUser.getName(), items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals("govhub_users_viewer", items.getJsonObject(1).getJsonObject("role").getString("role_name"));
		assertEquals(0, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(0, items.getJsonObject(0).getJsonArray("services").size());
		
		// Cancellazione Autorizzazione
		this.mockMvc.perform(delete(Costanti.AUTHORIZATIONS_BASE_PATH_DETAIL_ID, user.getId().intValue(), idRole)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isOk())
				.andReturn();
		
		// Verifica che la lista autorizzazioni sia vuota
		result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
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
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		assertEquals("govhub_users_viewer", items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals(0, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(0, items.getJsonObject(0).getJsonArray("services").size());
	}
	
	@Test
	/*
	 * Con l'admin assegno all'utenza SNakamoto la possibilita' di editare i ruoli per l'ente[3]
	 * Con l'utenza SNakamoto edito i ruoli dell'utenza user_viewer assegnado il ruolo user_viewer per l'ente[3].
	 * */
	void UC_4_05_DeleteAuthorizationOk_UserEditor_Organization() throws Exception {
		configurazioneDB();
		// Assegno all'utenza SNakamoto la possibilita' di editare i ruoli
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_users_editor");
		
		OffsetDateTime expirationDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
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
				.andExpect(jsonPath("$.role.role_name", is("govhub_users_editor")))
				.andExpect(jsonPath("$.organizations[0].tax_code", is(ente.getTaxCode())))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		user = leggiUtenteDB("user_viewer");
		
		ruoloUser = leggiRuoloDB("govhub_user");
		
		json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		result = this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect(jsonPath("$.organizations[0].tax_code", is(ente.getTaxCode())))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole = reader.readObject().getInt("id");
		
		result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
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
		
		assertEquals(idRole, items.getJsonObject(0).getInt("id"));
		assertEquals(ruoloUser.getName(), items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals("govhub_users_viewer", items.getJsonObject(1).getJsonObject("role").getString("role_name"));
		assertEquals(1, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(0, items.getJsonObject(0).getJsonArray("services").size());
		assertEquals(ente.getTaxCode(), items.getJsonObject(0).getJsonArray("organizations").getJsonObject(0).getString("tax_code"));
		
		// Cancellazione Autorizzazione
		this.mockMvc.perform(delete(Costanti.AUTHORIZATIONS_BASE_PATH_DETAIL_ID, user.getId().intValue(), idRole)
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isOk())
				.andReturn();
		
		// Verifica che la lista autorizzazioni sia vuota
		result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
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
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		assertEquals("govhub_users_viewer", items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals(0, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(0, items.getJsonObject(0).getJsonArray("services").size());
	}
	
	@Test
	/*
	 * Con l'admin assegno all'utenza SNakamoto la possibilita' di editare i ruoli per tutti gli enti
	 * Con l'utenza SNakamoto edito i ruoli dell'utenza user_viewer assegnado il ruolo user_viewer per l'ente[3].
	 * */
	void UC_4_06_DeleteAuthorizationOk_UserEditor_Organization_Ente3() throws Exception {
		configurazioneDB();
		// Assegno all'utenza SNakamoto la possibilita' di editare i ruoli
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_users_editor");
		
		OffsetDateTime expirationDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
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
				.andExpect(jsonPath("$.role.role_name", is("govhub_users_editor")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		user = leggiUtenteDB("user_viewer");
		
		ruoloUser = leggiRuoloDB("govhub_user");
		
		json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		result = this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect(jsonPath("$.organizations[0].tax_code", is(ente.getTaxCode())))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole = reader.readObject().getInt("id");
		
		result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
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
		
		assertEquals(idRole, items.getJsonObject(0).getInt("id"));
		assertEquals(ruoloUser.getName(), items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals("govhub_users_viewer", items.getJsonObject(1).getJsonObject("role").getString("role_name"));
		assertEquals(1, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(0, items.getJsonObject(0).getJsonArray("services").size());
		assertEquals(ente.getTaxCode(), items.getJsonObject(0).getJsonArray("organizations").getJsonObject(0).getString("tax_code"));
		
		// Cancellazione Autorizzazione
		this.mockMvc.perform(delete(Costanti.AUTHORIZATIONS_BASE_PATH_DETAIL_ID, user.getId().intValue(), idRole)
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isOk())
				.andReturn();
		
		// Verifica che la lista autorizzazioni sia vuota
		result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
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
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		assertEquals("govhub_users_viewer", items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals(0, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(0, items.getJsonObject(0).getJsonArray("services").size());
	}
	
	@Test
	/*
	 * Con l'admin assegno all'utenza SNakamoto la possibilita' di editare i ruoli per il servizio[2]
	 * Con l'utenza SNakamoto edito i ruoli dell'utenza user_viewer assegnado il ruolo user_viewer per il servizio[2]
	 * */
	void UC_4_07_DeleteAuthorizationOk_UserEditor_Service() throws Exception {
		configurazioneDB();
		// Assegno all'utenza SNakamoto la possibilita' di editare i ruoli
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_users_editor");
		
		OffsetDateTime expirationDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
				.add("expiration_date", dt.format(expirationDate))
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
				.andExpect(jsonPath("$.role.role_name", is("govhub_users_editor")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
				.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		user = leggiUtenteDB("user_viewer");
		
		ruoloUser = leggiRuoloDB("govhub_user");
		
		json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
//				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		result = this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
				.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
//				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole = reader.readObject().getInt("id");
		
		result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
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
		
		assertEquals(idRole, items.getJsonObject(0).getInt("id"));
		assertEquals(ruoloUser.getName(), items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals("govhub_users_viewer", items.getJsonObject(1).getJsonObject("role").getString("role_name"));
		assertEquals(0, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(1, items.getJsonObject(0).getJsonArray("services").size());
		assertEquals(servizio.getName(), items.getJsonObject(0).getJsonArray("services").getJsonObject(0).getString("service_name"));
		
		// Cancellazione Autorizzazione
		this.mockMvc.perform(delete(Costanti.AUTHORIZATIONS_BASE_PATH_DETAIL_ID, user.getId().intValue(), idRole)
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isOk())
				.andReturn();
		
		// Verifica che la lista autorizzazioni sia vuota
		result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
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
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		assertEquals("govhub_users_viewer", items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals(0, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(0, items.getJsonObject(0).getJsonArray("services").size());
	}
	
	@Test
	/*
	 * Con l'admin assegno all'utenza SNakamoto la possibilita' di editare i ruoli per tutti i servizi
	 * Con l'utenza SNakamoto edito i ruoli dell'utenza user_viewer assegnado il ruolo user_viewer per il servizio2.
	 * */
	void UC_4_08_DeleteAuthorizationOk_UserEditor_Service_Servizio2() throws Exception {
		configurazioneDB();
		// Assegno all'utenza SNakamoto la possibilita' di editare i ruoli
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_users_editor");
		
		OffsetDateTime expirationDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
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
				.andExpect(jsonPath("$.role.role_name", is("govhub_users_editor")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		user = leggiUtenteDB("user_viewer");
		
		ruoloUser = leggiRuoloDB("govhub_user");
		
		json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		result = this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
				.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole = reader.readObject().getInt("id");
		
		result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
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
		
		assertEquals(idRole, items.getJsonObject(0).getInt("id"));
		assertEquals(ruoloUser.getName(), items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals("govhub_users_viewer", items.getJsonObject(1).getJsonObject("role").getString("role_name"));
		assertEquals(0, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(1, items.getJsonObject(0).getJsonArray("services").size());
		assertEquals(servizio.getName(), items.getJsonObject(0).getJsonArray("services").getJsonObject(0).getString("service_name"));
		
		// Cancellazione Autorizzazione
		this.mockMvc.perform(delete(Costanti.AUTHORIZATIONS_BASE_PATH_DETAIL_ID, user.getId().intValue(), idRole)
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isOk())
				.andReturn();
		
		// Verifica che la lista autorizzazioni sia vuota
		result = this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
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
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		assertEquals("govhub_users_viewer", items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals(0, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(0, items.getJsonObject(0).getJsonArray("services").size());
	}
	
	private void deleteAllAuthorizations(UserEntity user) throws Exception {
		Utils.deleteAllAuthorizations(user, this.mockMvc, this.userAuthProfilesUtils.utenzaAdmin());
	}
}
