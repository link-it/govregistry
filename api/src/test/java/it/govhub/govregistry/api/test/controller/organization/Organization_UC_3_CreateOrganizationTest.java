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
import it.govhub.govregistry.api.repository.OrganizationRepository;
import it.govhub.govregistry.api.test.Costanti;
import it.govhub.govregistry.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.OrganizationEntity;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di creazione delle Organizations")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Organization_UC_3_CreateOrganizationTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	

	@Test
	void UC_3_01_CreateOrganizationOk() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();
		ente.setTaxCode("30145678903");
		ente.setLegalName(ente.getLegalName() + "-301");

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
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
	
	@Test
	void UC_3_02_CreateOrganizationOk_withOfficeProps() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();
		ente.setTaxCode("30245678903");
		ente.setLegalName(ente.getLegalName() + "-302");

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
				.add("office_zip", ente.getOfficeZip())
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(Costanti.ORGANIZATIONS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.office_address", is(ente.getOfficeAddress())))
				.andExpect(jsonPath("$.office_address_details", is(ente.getOfficeAddressDetails())))
				.andExpect(jsonPath("$.office_at", is(ente.getOfficeAt())))
				.andExpect(jsonPath("$.office_email_address", is(ente.getOfficeEmailAddress())))
				.andExpect(jsonPath("$.office_foreign_state", is(ente.getOfficeForeignState())))
				.andExpect(jsonPath("$.office_municipality", is(ente.getOfficeMunicipality())))
				.andExpect(jsonPath("$.office_municipality_details", is(ente.getOfficeMunicipalityDetails())))
				.andExpect(jsonPath("$.office_pec_address", is(ente.getOfficePecAddress())))
				.andExpect(jsonPath("$.office_phone_number", is(ente.getOfficePhoneNumber())))
				.andExpect(jsonPath("$.office_province", is(ente.getOfficeProvince())))
				.andExpect(jsonPath("$.office_zip", is(ente.getOfficeZip())))
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
		assertEquals(ente.getOfficeAddress(), organizationEntity.getOfficeAddress());
		assertEquals(ente.getOfficeAddressDetails(), organizationEntity.getOfficeAddressDetails());
		assertEquals(ente.getOfficeAt(), organizationEntity.getOfficeAt());
		assertEquals(ente.getOfficeEmailAddress(), organizationEntity.getOfficeEmailAddress());
		assertEquals(ente.getOfficeForeignState(), organizationEntity.getOfficeForeignState());
		assertEquals(ente.getOfficeMunicipality(), organizationEntity.getOfficeMunicipality());
		assertEquals(ente.getOfficeMunicipalityDetails(), organizationEntity.getOfficeMunicipalityDetails());
		assertEquals(ente.getOfficePecAddress(), organizationEntity.getOfficePecAddress());
		assertEquals(ente.getOfficePhoneNumber(), organizationEntity.getOfficePhoneNumber());
		assertEquals(ente.getOfficeProvince(), organizationEntity.getOfficeProvince());
		assertEquals(ente.getOfficeZip(), organizationEntity.getOfficeZip());
		
	}
}
