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
package it.govhub.govregistry.api.test.controller.organization;

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
import it.govhub.govregistry.api.repository.OrganizationRepository;
import it.govhub.govregistry.api.repository.ServiceRepository;
import it.govhub.govregistry.api.repository.UserRepository;
import it.govhub.govregistry.api.test.Costanti;
import it.govhub.govregistry.api.test.utils.Matchers;
import it.govhub.govregistry.api.test.utils.Utils;
import it.govhub.govregistry.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.api.beans.PatchOp.OpEnum;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.RoleEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.repository.ReadRoleRepository;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di controllo autorizzazioni necessarie all'esecuzione delle operazioni sulle Organizations")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Organization_UC_6_AutorizzazioneUtenzeTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ServiceRepository serviceRepository;
	
	@Autowired
	public ReadRoleRepository roleRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	private void configurazioneDB() {
		UserEntity user = Costanti.getUser_Snakamoto();
		if(leggiUtenteDB(user.getPrincipal()) == null) {
			this.userRepository.save(user);
		}
		
		OrganizationEntity ente = Costanti.getEnteCreditore4();
		if(leggiEnteDB(ente.getTaxCode()) == null) {
			this.organizationRepository.save(ente);
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
	
	//1. CreateOrganization con utenza non admin con ruolo govhub_organizations_editor: OK
	@Test
	void UC_6_01_CreateOrganizationOk_UtenzaConRuolo_GovHub_Organizations_Editor() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();
		ente.setTaxCode("60145678903");
		ente.setLegalName(ente.getLegalName() + "-601");

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaOrganizationEditor())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();
		
		// Leggo l'organization dal servizio e verifico
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		OrganizationEntity organizationEntity = this.organizationRepository.findById((long) id).get();
		
		assertEquals(id, organizationEntity.getId());
		assertEquals(ente.getTaxCode(), organizationEntity.getTaxCode());
		assertEquals(ente.getLegalName(), organizationEntity.getLegalName());
		assertNull(organizationEntity.getLogo());
		assertNull(organizationEntity.getLogoMiniature());
		assertNull(organizationEntity.getOfficeAddress());
		assertNull(organizationEntity.getOfficeAddressDetails());
		assertNull(organizationEntity.getOfficeAt());
		assertNull(organizationEntity.getOfficeEmailAddress());
		assertNull(organizationEntity.getOfficeForeignState());
		assertNull(organizationEntity.getOfficeMunicipality());
		assertNull(organizationEntity.getOfficeMunicipalityDetails());
		assertNull(organizationEntity.getOfficePecAddress());
		assertNull(organizationEntity.getOfficePhoneNumber());
		assertNull(organizationEntity.getOfficeProvince());
		assertNull(organizationEntity.getOfficeZip());
	}
	
	//2. CreateOrganization con utenza non admin con ruolo non govhub_organizations_editor: NotAuthorized
	@Test
	void UC_6_02_CreateOrganizationFail_UtenzaSenzaRuolo_GovHub_Organizations_Editor() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaOrganizationViewer())
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
	
	//3. PatchOrganization con utenza non admin con ruolo govhub_organizations_editor: OK
	@Test
	void UC_6_03_PatchOrganizationOk_UtenzaConRuolo_GovHub_Organizations_Editor() throws Exception {
		configurazioneDB();		
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_4);
		long id = ente.getId();

		try {
			// Modifico l'organization
			ente.setTaxCode("12345678907");
	
			JsonObjectBuilder patchOp = Json.createObjectBuilder()
					.add("op", OpEnum.REPLACE.toString())
					.add("path", "/tax_code")
					.add("value", ente.getTaxCode());
	
			String PatchOrganization = Json.createArrayBuilder()
					.add(patchOp)
					.build()
					.toString();
	
			this.mockMvc.perform(patch(Costanti.ORGANIZATIONS_BASE_PATH_DETAIL_ID, id)
					.with(this.userAuthProfilesUtils.utenzaOrganizationEditor())
					.with(csrf())
					.content(PatchOrganization)
					.contentType("application/json-patch+json")
					.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
			.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
			.andReturn();
	
			OrganizationEntity organizationEntity = this.organizationRepository.findById((long) id).get();
	
			assertEquals(id, organizationEntity.getId());
			assertEquals(ente.getTaxCode(), organizationEntity.getTaxCode());
			assertEquals(ente.getLegalName(), organizationEntity.getLegalName());
			assertNull(organizationEntity.getLogo());
			assertNull(organizationEntity.getLogoMiniature());
			assertNull(organizationEntity.getOfficeAddress());
			assertNull(organizationEntity.getOfficeAddressDetails());
			assertNull(organizationEntity.getOfficeAt());
			assertNull(organizationEntity.getOfficeEmailAddress());
			assertNull(organizationEntity.getOfficeForeignState());
			assertNull(organizationEntity.getOfficeMunicipality());
			assertNull(organizationEntity.getOfficeMunicipalityDetails());
			assertNull(organizationEntity.getOfficePecAddress());
			assertNull(organizationEntity.getOfficePhoneNumber());
			assertNull(organizationEntity.getOfficeProvince());
			assertNull(organizationEntity.getOfficeZip());
		}finally {
			// ripristino
			ente.setTaxCode(Costanti.TAX_CODE_ENTE_CREDITORE_4);
			
			JsonObjectBuilder patchOp = Json.createObjectBuilder()
					.add("op", OpEnum.REPLACE.toString())
					.add("path", "/tax_code")
					.add("value", ente.getTaxCode());
	
			String PatchOrganization = Json.createArrayBuilder()
					.add(patchOp)
					.build()
					.toString();
	
			this.mockMvc.perform(patch(Costanti.ORGANIZATIONS_BASE_PATH_DETAIL_ID, id)
					.with(this.userAuthProfilesUtils.utenzaOrganizationEditor())
					.with(csrf())
					.content(PatchOrganization)
					.contentType("application/json-patch+json")
					.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
			.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
			.andReturn();
		}
	}
	
	//4. PatchOrganization con utenza non admin con ruolo non govhub_organizations_editor: NotAuthorized
	@Test
	void UC_6_04_PatchOrganizationFail_UtenzaSenzaRuolo_GovHub_Organizations_Editor() throws Exception {
		configurazioneDB();		
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		long id = ente.getId();

		// Modifico l'organization
		ente.setTaxCode("12345678907");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/tax_code")
				.add("value", ente.getTaxCode());

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(Costanti.ORGANIZATIONS_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaOrganizationViewer())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();

	}
	
	//5. FindAllOrganizations con utenza non admin con ruolo govhub_organizations_editor/govhub_organizations_viewer: OK
	@Test
	void UC_6_05_FindAllOk_UtenzaConRuolo_GovHub_Organizations_Editor_O_Viewer() throws Exception {
		this.mockMvc.perform(get(Costanti.ORGANIZATIONS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaOrganizationEditor())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		this.mockMvc.perform(get(Costanti.ORGANIZATIONS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaOrganizationViewer())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
	}
	
	//6. FindAllOrganizations con utenza non admin con ruolo non govhub_organizations_editor/govhub_organizations_viewer: NotAuthorized
	@Test
	void UC_6_06_FindAllFail_UtenzaSenzaRuolo_GovHub_Organizations_Editor_O_Viewer() throws Exception {
		
		MvcResult result = this.mockMvc.perform(get(Costanti.ORGANIZATIONS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaOspite())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject orgList = reader.readObject();
		
		// Controlli sugli items
		JsonArray items = orgList.getJsonArray("items");
		assertEquals(0, items.size());
	}
	
	//7. GetOrganization con utenza non admin con ruolo govhub_organizations_editor/govhub_organizations_viewer: OK
	@Test
	void UC_6_07_GetOrganizationOk_UtenzaConRuolo_GovHub_Organizations_Editor_O_Viewer() throws Exception {
		MvcResult result = this.mockMvc.perform(get(Costanti.ORGANIZATIONS_BASE_PATH)
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
		
		this.mockMvc.perform(get(Costanti.ORGANIZATIONS_BASE_PATH_DETAIL_ID,idUser1)
				.with(this.userAuthProfilesUtils.utenzaOrganizationEditor())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		this.mockMvc.perform(get(Costanti.ORGANIZATIONS_BASE_PATH_DETAIL_ID,idUser1)
				.with(this.userAuthProfilesUtils.utenzaOrganizationViewer())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
	}
	
	//8. GetOrganization con utenza non admin con ruolo non govhub_organizations_editor/govhub_organizations_viewer: NotAuthorized
	@Test
	void UC_6_08_GetOrganizationFail_UtenzaSenzaRuolo_GovHub_Organizations_Editor_O_Viewer() throws Exception {
		MvcResult result = this.mockMvc.perform(get(Costanti.ORGANIZATIONS_BASE_PATH)
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
		
		this.mockMvc.perform(get(Costanti.ORGANIZATIONS_BASE_PATH_DETAIL_ID,idUser1)
				.with(this.userAuthProfilesUtils.utenzaOspite())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	//9. FindAllOrganizations con utenza non admin con ruolo govhub_organizations_editor/govhub_organizations_viewer: OK che ha autorizzazione solo su una organization
	@Test
	void UC_6_09_FindAllOk_UtenzaConRuolo_GovHub_Organizations_Editor_O_Viewer_Authorization() throws Exception {
		configurazioneDB();
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_4);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		RoleEntity ruoloUser = leggiRuoloDB("govhub_organizations_viewer");
		
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder())
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
				.andExpect(jsonPath("$.role.role_name", is("govhub_organizations_viewer")))
				.andExpect(jsonPath("$.organizations[0].tax_code", is(ente.getTaxCode())))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
				.andReturn();
		
		// leggo la lista delle organizations con l'utenza che puo' visualizzarne solo 1
		result = this.mockMvc.perform(get(Costanti.ORGANIZATIONS_BASE_PATH)
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
		
		assertEquals(ente.getTaxCode(), items.getJsonObject(0).getString("tax_code"));
	}
	
	//10. GetOrganization con utenza non admin con ruolo govhub_organizations_viewer che ha autorizzazione solo su una organization
	@Test
	void UC_6_10_GetOrganizationOk_UtenzaConRuolo_GovHub_Organizations_Viewer_Authorization() throws Exception {
		configurazioneDB();
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_4);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		RoleEntity ruoloUser = leggiRuoloDB("govhub_organizations_viewer");
		
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder())
				.build()
				.toString();
		
		// Autorizzo SNakamoto a vedere l'organization Ente4 
		MvcResult result = this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_organizations_viewer")))
				.andExpect(jsonPath("$.organizations[0].tax_code", is(ente.getTaxCode())))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
				.andReturn();
		
		result = this.mockMvc.perform(get(Costanti.ORGANIZATIONS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		JsonArray items = userList.getJsonArray("items");
		
		JsonObject item1 = items.getJsonObject(items.size() - 1);  // primo ente creato non Ente4
		int idEnte1 = item1.getInt("id"); // Ente1
				
		// l'utenza non vede altre organization a parte quella che gli e' stata assegnata (Ente 4)
		this.mockMvc.perform(get(Costanti.ORGANIZATIONS_BASE_PATH_DETAIL_ID,idEnte1)
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
		// l'utenza puo' vedere solo l'organization che gli e' stata assegnata (Ente 4)
		this.mockMvc.perform(get(Costanti.ORGANIZATIONS_BASE_PATH_DETAIL_ID, ente.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
	}
	
	//11. PatchOrganization con utenza non admin con ruolo govhub_organizations_editor che ha autorizzazione solo su una organization
	//@Test
	void UC_6_11_PatchOrganizationOk_UtenzaConRuolo_GovHub_Organizations_Editor_Authorization() throws Exception {
		configurazioneDB();
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_4);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		RoleEntity ruoloUser = leggiRuoloDB("govhub_organizations_editor");
		
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder())
				.build()
				.toString();
		
		// Autorizzo SNakamoto a vedere l'organization Ente4 
		MvcResult result = this.mockMvc.perform(post(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_organizations_editor")))
				.andExpect(jsonPath("$.organizations[0].tax_code", is(ente.getTaxCode())))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
				.andReturn();
		
		// Prendo la lista delle organization 
		result = this.mockMvc.perform(get(Costanti.ORGANIZATIONS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		JsonArray items = userList.getJsonArray("items");
		
		JsonObject item1 = items.getJsonObject(items.size() - 1);  // primo ente creato non Ente4
		int idEnte1 = item1.getInt("id"); // Ente1
		
		// Modifico l'organization 4
		ente.setTaxCode("12345678907");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/tax_code")
				.add("value", ente.getTaxCode());

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(Costanti.ORGANIZATIONS_BASE_PATH_DETAIL_ID, ente.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
		.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
		.andReturn();

		OrganizationEntity organizationEntity = this.organizationRepository.findById((long) ente.getId()).get();

		assertEquals(ente.getId(), organizationEntity.getId());
		assertEquals(ente.getTaxCode(), organizationEntity.getTaxCode());
		assertEquals(ente.getLegalName(), organizationEntity.getLegalName());
		assertNull(organizationEntity.getLogo());
		assertNull(organizationEntity.getLogoMiniature());
		assertNull(organizationEntity.getOfficeAddress());
		assertNull(organizationEntity.getOfficeAddressDetails());
		assertNull(organizationEntity.getOfficeAt());
		assertNull(organizationEntity.getOfficeEmailAddress());
		assertNull(organizationEntity.getOfficeForeignState());
		assertNull(organizationEntity.getOfficeMunicipality());
		assertNull(organizationEntity.getOfficeMunicipalityDetails());
		assertNull(organizationEntity.getOfficePecAddress());
		assertNull(organizationEntity.getOfficePhoneNumber());
		assertNull(organizationEntity.getOfficeProvince());
		assertNull(organizationEntity.getOfficeZip());
		
		// Patch su un organization per cui non si e' autorizzati fallisce
		this.mockMvc.perform(patch(Costanti.ORGANIZATIONS_BASE_PATH_DETAIL_ID, idEnte1)
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.content(PatchOrganization)
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

