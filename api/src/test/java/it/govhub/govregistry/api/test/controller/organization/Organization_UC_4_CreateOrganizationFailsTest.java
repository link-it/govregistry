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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.json.Json;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;

import it.govhub.govregistry.api.Application;
import it.govhub.govregistry.api.test.Costanti;
import it.govhub.govregistry.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.OrganizationEntity;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di validazione delle creazioni delle Organizations")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Organization_UC_4_CreateOrganizationFailsTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Test
	void UC_4_01_CreateOrganizationFail_MissingTaxCode() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
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
	void UC_4_02_CreateOrganizationFail_MissingLegalName() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
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
	void UC_4_03_CreateOrganizationFail_OfficeAddressTooLong() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_address", Costanti.STRING_256)
				.add("office_address_details", ente.getOfficeAddressDetails())
				.add("office_at", ente.getOfficeAt())
				.add("office_email_address", ente.getOfficeEmailAddress())
				.add("office_foreign_state", ente.getOfficeForeignState())
				.add("office_municipality", ente.getOfficeMunicipality())
				.add("office_municipality_details", ente.getOfficeMunicipalityDetails())
				.add("office_pec_address", ente.getOfficePecAddress())
				.add("office_phone_number", ente.getOfficePhoneNumber())
				.add("office_province", ente.getOfficeProvince())
				.add("office_zip", ente.getOfficeZip())
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
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
	void UC_4_04_CreateOrganizationFail_OfficeAddressDetailsTooLong() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_address", ente.getOfficeAddress())
				.add("office_address_details", Costanti.STRING_256)
				.add("office_at", ente.getOfficeAt())
				.add("office_email_address", ente.getOfficeEmailAddress())
				.add("office_foreign_state", ente.getOfficeForeignState())
				.add("office_municipality", ente.getOfficeMunicipality())
				.add("office_municipality_details", ente.getOfficeMunicipalityDetails())
				.add("office_pec_address", ente.getOfficePecAddress())
				.add("office_phone_number", ente.getOfficePhoneNumber())
				.add("office_province", ente.getOfficeProvince())
				.add("office_zip", ente.getOfficeZip())
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
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
	void UC_4_05_CreateOrganizationFail_OfficeAtTooLong() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_address", ente.getOfficeAddress())
				.add("office_address_details", ente.getOfficeAddressDetails())
				.add("office_at", Costanti.STRING_256)
				.add("office_email_address", ente.getOfficeEmailAddress())
				.add("office_foreign_state", ente.getOfficeForeignState())
				.add("office_municipality", ente.getOfficeMunicipality())
				.add("office_municipality_details", ente.getOfficeMunicipalityDetails())
				.add("office_pec_address", ente.getOfficePecAddress())
				.add("office_phone_number", ente.getOfficePhoneNumber())
				.add("office_province", ente.getOfficeProvince())
				.add("office_zip", ente.getOfficeZip())
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
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
	void UC_4_06_CreateOrganizationFail_OfficeZipTooLong() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_address", ente.getOfficeAddress())
				.add("office_address_details", ente.getOfficeAddressDetails())
				.add("office_at", ente.getOfficeAt())
				.add("office_email_address", ente.getOfficeEmailAddress())
				.add("office_foreign_state", ente.getOfficeForeignState())
				.add("office_municipality", ente.getOfficeMunicipality())
				.add("office_municipality_details", ente.getOfficeMunicipalityDetails())
				.add("office_pec_address", ente.getOfficePecAddress())
				.add("office_phone_number", ente.getOfficePhoneNumber())
				.add("office_province", ente.getOfficeProvince())
				.add("office_zip", Costanti.STRING_256)
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
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
	void UC_4_07_CreateOrganizationFail_OfficeMunicipalityTooLong() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_address", ente.getOfficeAddress())
				.add("office_address_details", ente.getOfficeAddressDetails())
				.add("office_at", ente.getOfficeAt())
				.add("office_email_address", ente.getOfficeEmailAddress())
				.add("office_foreign_state", ente.getOfficeForeignState())
				.add("office_municipality", Costanti.STRING_256)
				.add("office_municipality_details", ente.getOfficeMunicipalityDetails())
				.add("office_pec_address", ente.getOfficePecAddress())
				.add("office_phone_number", ente.getOfficePhoneNumber())
				.add("office_province", ente.getOfficeProvince())
				.add("office_zip", ente.getOfficeZip())
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
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
	void UC_4_08_CreateOrganizationFail_OfficeMunicipalityDetailsTooLong() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_address", ente.getOfficeAddress())
				.add("office_address_details", ente.getOfficeAddressDetails())
				.add("office_at", ente.getOfficeAt())
				.add("office_email_address", ente.getOfficeEmailAddress())
				.add("office_foreign_state", ente.getOfficeForeignState())
				.add("office_municipality", ente.getOfficeMunicipality())
				.add("office_municipality_details", Costanti.STRING_256)
				.add("office_pec_address", ente.getOfficePecAddress())
				.add("office_phone_number", ente.getOfficePhoneNumber())
				.add("office_province", ente.getOfficeProvince())
				.add("office_zip", ente.getOfficeZip())
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
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
	void UC_4_09_CreateOrganizationFail_OfficeProvinceTooLong() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_address", ente.getOfficeAddress())
				.add("office_address_details", ente.getOfficeAddressDetails())
				.add("office_at", ente.getOfficeAt())
				.add("office_email_address", ente.getOfficeEmailAddress())
				.add("office_foreign_state", ente.getOfficeForeignState())
				.add("office_municipality", ente.getOfficeMunicipality())
				.add("office_municipality_details", ente.getOfficeMunicipalityDetails())
				.add("office_pec_address", ente.getOfficePecAddress())
				.add("office_phone_number", ente.getOfficePhoneNumber())
				.add("office_province", Costanti.STRING_256)
				.add("office_zip", ente.getOfficeZip())
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
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
	void UC_4_10_CreateOrganizationFail_OfficeForeignStateTooLong() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_address", ente.getOfficeAddress())
				.add("office_address_details", ente.getOfficeAddressDetails())
				.add("office_at", ente.getOfficeAt())
				.add("office_email_address", ente.getOfficeEmailAddress())
				.add("office_foreign_state", Costanti.STRING_256)
				.add("office_municipality", ente.getOfficeMunicipality())
				.add("office_municipality_details", ente.getOfficeMunicipalityDetails())
				.add("office_pec_address", ente.getOfficePecAddress())
				.add("office_phone_number", ente.getOfficePhoneNumber())
				.add("office_province", ente.getOfficeProvince())
				.add("office_zip", ente.getOfficeZip())
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
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
	void UC_4_11_CreateOrganizationFail_OfficePhoneNumberTooLong() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_address", ente.getOfficeAddress())
				.add("office_address_details", ente.getOfficeAddressDetails())
				.add("office_at", ente.getOfficeAt())
				.add("office_email_address", ente.getOfficeEmailAddress())
				.add("office_foreign_state", ente.getOfficeForeignState())
				.add("office_municipality", ente.getOfficeMunicipality())
				.add("office_municipality_details", ente.getOfficeMunicipalityDetails())
				.add("office_pec_address", ente.getOfficePecAddress())
				.add("office_phone_number", Costanti.STRING_256)
				.add("office_province", ente.getOfficeProvince())
				.add("office_zip", ente.getOfficeZip())
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
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
	void UC_4_12_CreateOrganizationFail_OfficeEmailAddressTooLong() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_address", ente.getOfficeAddress())
				.add("office_address_details", ente.getOfficeAddressDetails())
				.add("office_at", ente.getOfficeAt())
				.add("office_email_address", Costanti.STRING_256)
				.add("office_foreign_state", ente.getOfficeForeignState())
				.add("office_municipality", ente.getOfficeMunicipality())
				.add("office_municipality_details", ente.getOfficeMunicipalityDetails())
				.add("office_pec_address", ente.getOfficePecAddress())
				.add("office_phone_number", ente.getOfficePhoneNumber())
				.add("office_province", ente.getOfficeProvince())
				.add("office_zip", ente.getOfficeZip())
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
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
	void UC_4_13_CreateOrganizationFail_OfficePecAddressTooLong() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_address", ente.getOfficeAddress())
				.add("office_address_details", ente.getOfficeAddressDetails())
				.add("office_at", ente.getOfficeAt())
				.add("office_email_address", ente.getOfficeEmailAddress())
				.add("office_foreign_state", ente.getOfficeForeignState())
				.add("office_municipality", ente.getOfficeMunicipality())
				.add("office_municipality_details", ente.getOfficeMunicipalityDetails())
				.add("office_pec_address", Costanti.STRING_256)
				.add("office_phone_number", ente.getOfficePhoneNumber())
				.add("office_province", ente.getOfficeProvince())
				.add("office_zip", ente.getOfficeZip())
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
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
	void UC_4_14_CreateOrganizationFail_InvalidOfficePhoneNumber() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_address", ente.getOfficeAddress())
				.add("office_address_details", ente.getOfficeAddressDetails())
				.add("office_at", ente.getOfficeAt())
				.add("office_email_address", ente.getOfficeEmailAddress())
				.add("office_foreign_state", ente.getOfficeForeignState())
				.add("office_municipality", ente.getOfficeMunicipality())
				.add("office_municipality_details", ente.getOfficeMunicipalityDetails())
				.add("office_pec_address", ente.getOfficePecAddress())
				.add("office_phone_number", "XXX")
				.add("office_province", ente.getOfficeProvince())
				.add("office_zip", ente.getOfficeZip())
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
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
	void UC_4_15_CreateOrganizationFail_ConflictTaxCode() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();
		ente.setTaxCode("41545678901");
		ente.setLegalName("Ente Test 415");

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();
		
		json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", "LegalName4")
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status", is(409)))
				.andExpect(jsonPath("$.title", is("Conflict")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	@Test
	void UC_4_16_CreateOrganizationFail_ConflictLegalName() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();
		ente.setTaxCode("41645678901");
		ente.setLegalName("Ente Test 416");

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();
		
		json = Json.createObjectBuilder()
				.add("tax_code", "12345678904")
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status", is(409)))
				.andExpect(jsonPath("$.title", is("Conflict")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
}
