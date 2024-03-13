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
import it.govhub.govregistry.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.api.test.utils.Utils;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.RoleEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.repository.ReadRoleRepository;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di controllo autorizzazioni sulle operazioni delle Authorization")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Authorization_UC_6_AutorizzazioniUtenzeTest {
	
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
	
	// 1. CreateAuthorization con utenza non admin con ruolo users_editor: Ok
	@Test
	void UC_6_01_CreateAuthorizationOk_UtenzaConRuolo_GovHub_Users_Editor() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		String ruoloDaAssegnare = "govhub_user";
		RoleEntity ruoloUser = leggiRuoloDB(ruoloDaAssegnare);
		
		OffsetDateTime expirationDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		
		// Creo una authorization e verifico la risposta
		this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaUserEditor())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is(ruoloDaAssegnare)))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
	}
	
	// 2. CreateAuthorization con utenza non admin con ruolo non users_editor: NotAuthorized
	@Test
	void UC_6_02_CreateAuthorizationFail_UtenzaSenzaRuolo_GovHub_Users_Editor() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		String ruoloDaAssegnare = "govhub_user";
		RoleEntity ruoloUser = leggiRuoloDB(ruoloDaAssegnare);
		
		OffsetDateTime expirationDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una authorization e verifico la risposta
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
	
	// 3. FindAllAuthorizations con utenza non admin con ruolo users_editor o users_viewer: Ok
	@Test
	void UC_6_03_FindAllOk_UtenzaConRuolo_GovHub_Users_Editor() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaUserEditor())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaUserViewer())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
	}
	
	// 4. FindAllAuthorizations con utenza non admin con ruolo non users_editor o users_viewer: NotAuthorized
	@Test
	void UC_6_04_FindAllFail_UtenzaSenzaRuolo_GovHub_Users_Editor() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		this.mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaOspite())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	// 5. DeleteAuthorization con utenza non admin con ruolo users_editor: Ok
	@Test
	void UC_6_05_DeleteAuthorizationOk_UtenzaConRuolo_GovHub_Users_Editor() throws Exception {
		configurazioneDB();
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		String ruoloDaAssegnare = "govhub_user";
		RoleEntity ruoloUser = leggiRuoloDB(ruoloDaAssegnare);
		
		OffsetDateTime expirationDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una authorization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaUserEditor())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is(ruoloDaAssegnare)))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole = reader.readObject().getInt("id");
		
		// Cancellazione Autorizzazione
		this.mockMvc.perform(delete(Costanti.AUTHORIZATIONS_BASE_PATH_DETAIL_ID, user.getId().intValue(), idRole)
				.with(this.userAuthProfilesUtils.utenzaUserEditor())
				.with(csrf())
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
}
