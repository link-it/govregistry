package it.govhub.rest.backoffice.test.controller.organization;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import it.govhub.rest.backoffice.Application;
import it.govhub.rest.backoffice.beans.PatchOp.OpEnum;
import it.govhub.rest.backoffice.entity.OrganizationEntity;
import it.govhub.rest.backoffice.repository.OrganizationRepository;
import it.govhub.rest.backoffice.test.Costanti;
import it.govhub.rest.backoffice.test.utils.UserAuthProfilesUtils;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di censimento Organization")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

public class Organization_UC_5_PatchOrganizationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OrganizationRepository organizationRepository;


	@Test
	void UC_5_01_PatchOrganization_WrongMediaType() throws Exception {
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/tax_code")
				.add("value", "12345678901");

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", 1)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().is(400))
		.andExpect(jsonPath("$.detail", is("Content type 'application/json' not supported")))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type", is("https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request")))
		.andReturn();
	}

	@Test
	void UC_5_02_PatchOrganization_WrongPayload() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Creo un messaggio di patch errato, la specifica prevede che venga inviato un'array di operazioni
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		String PatchOrganization = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/tax_code")
				.add("value", "12345678901")
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().is(400))
		.andExpect(jsonPath("$.detail").isString())
		.andExpect(jsonPath("$.type", is("https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request")))
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
	}

	@Test
	void UC_5_03_PatchOrganization_TaxCode() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		ente.setTaxCode("12345678907");

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/tax_code")
				.add("value", ente.getTaxCode());

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
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
	}

	@Test
	void UC_5_04_PatchOrganization_RemoveTaxCode() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization cancellando il tax code
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REMOVE.toString())
				.add("path", "/tax_code")
				.add("value", "");

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
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
	}

	@Test
	void UC_5_05_PatchOrganization_LegalName() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		ente.setLegalName("12345678907");

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/legal_name")
				.add("value", ente.getLegalName());

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
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
	}

	@Test
	void UC_5_06_PatchOrganization_RemoveLegalName() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization cancellando il legal name
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REMOVE.toString())
				.add("path", "/legal_name")
				.add("value", "");

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
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
	}

	@Test
	void UC_5_07_PatchOrganization_ReplaceOfficeAddress() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_address", ente.getOfficeAddress())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.office_address", is(ente.getOfficeAddress())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		// Modifico l'utente
		ente.setOfficeAddress("Parco della Vittoria 1/A");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/office_address")
				.add("value", ente.getOfficeAddress());

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
		.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
		.andExpect(jsonPath("$.office_address", is(ente.getOfficeAddress())))
		.andReturn();

		OrganizationEntity organizationEntity = this.organizationRepository.findById((long) id).get();

		assertEquals(id, organizationEntity.getId());
		assertEquals(ente.getTaxCode(), organizationEntity.getTaxCode());
		assertEquals(ente.getLegalName(), organizationEntity.getLegalName());
		assertNull(organizationEntity.getLogo());
		assertNull(organizationEntity.getLogoMiniature());
		assertEquals(ente.getOfficeAddress(), organizationEntity.getOfficeAddress());
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
	void UC_5_08_PatchOrganization_AddOfficeAddress() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		// Modifico l'utente
		ente.setOfficeAddress("Parco della Vittoria 1/A");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.ADD.toString())
				.add("path", "/office_address")
				.add("value", ente.getOfficeAddress());

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
		.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
		.andExpect(jsonPath("$.office_address", is(ente.getOfficeAddress())))
		.andReturn();

		OrganizationEntity organizationEntity = this.organizationRepository.findById((long) id).get();

		assertEquals(id, organizationEntity.getId());
		assertEquals(ente.getTaxCode(), organizationEntity.getTaxCode());
		assertEquals(ente.getLegalName(), organizationEntity.getLegalName());
		assertNull(organizationEntity.getLogo());
		assertNull(organizationEntity.getLogoMiniature());
		assertEquals(ente.getOfficeAddress(), organizationEntity.getOfficeAddress());
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
	void UC_5_09_PatchOrganization_RemoveOfficeAddress() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_address", ente.getOfficeAddress())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REMOVE.toString())
				.add("path", "/office_address")
				.add("value", "");

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
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
	}

	@Test
	void UC_5_10_PatchOrganization_ReplaceNotExistingOfficeAddress() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/office_address")
				.add("value", "Parco della Vittoria 1/A");

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
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
	}
	
	@Test
	void UC_5_11_PatchOrganization_ReplaceOfficeAddressDetails() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_address_details", ente.getOfficeAddressDetails())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.office_address_details", is(ente.getOfficeAddressDetails())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		// Modifico l'utente
		ente.setOfficeAddressDetails("Parco della Vittoria 1/A");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/office_address_details")
				.add("value", ente.getOfficeAddressDetails());

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
		.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
		.andExpect(jsonPath("$.office_address_details", is(ente.getOfficeAddressDetails())))
		.andReturn();

		OrganizationEntity organizationEntity = this.organizationRepository.findById((long) id).get();

		assertEquals(id, organizationEntity.getId());
		assertEquals(ente.getTaxCode(), organizationEntity.getTaxCode());
		assertEquals(ente.getLegalName(), organizationEntity.getLegalName());
		assertNull(organizationEntity.getLogo());
		assertNull(organizationEntity.getLogoMiniature());
		assertEquals(ente.getOfficeAddressDetails(), organizationEntity.getOfficeAddressDetails());
		assertNull(organizationEntity.getOfficeAddress());
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
	void UC_5_12_PatchOrganization_AddOfficeAddressDetails() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		// Modifico l'utente
		ente.setOfficeAddressDetails("Parco della Vittoria 1/A");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.ADD.toString())
				.add("path", "/office_address_details")
				.add("value", ente.getOfficeAddressDetails());

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
		.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
		.andExpect(jsonPath("$.office_address_details", is(ente.getOfficeAddressDetails())))
		.andReturn();

		OrganizationEntity organizationEntity = this.organizationRepository.findById((long) id).get();

		assertEquals(id, organizationEntity.getId());
		assertEquals(ente.getTaxCode(), organizationEntity.getTaxCode());
		assertEquals(ente.getLegalName(), organizationEntity.getLegalName());
		assertNull(organizationEntity.getLogo());
		assertNull(organizationEntity.getLogoMiniature());
		assertEquals(ente.getOfficeAddressDetails(), organizationEntity.getOfficeAddressDetails());
		assertNull(organizationEntity.getOfficeAddress());
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
	void UC_5_13_PatchOrganization_RemoveOfficeAddressDetails() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_address_details", ente.getOfficeAddressDetails())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REMOVE.toString())
				.add("path", "/office_address_details")
				.add("value", "");

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
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
	}

	@Test
	void UC_5_14_PatchOrganization_ReplaceNotExistingOfficeAddressDetails() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/office_address_details")
				.add("value", "Parco della Vittoria 1/A");

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
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
	}
	
	@Test
	void UC_5_15_PatchOrganization_ReplaceOfficeAt() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_at", ente.getOfficeAt())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.office_at", is(ente.getOfficeAt())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		// Modifico l'utente
		ente.setOfficeAt("Parco della Vittoria 1/A");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/office_at")
				.add("value", ente.getOfficeAt());

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
		.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
		.andExpect(jsonPath("$.office_at", is(ente.getOfficeAt())))
		.andReturn();

		OrganizationEntity organizationEntity = this.organizationRepository.findById((long) id).get();

		assertEquals(id, organizationEntity.getId());
		assertEquals(ente.getTaxCode(), organizationEntity.getTaxCode());
		assertEquals(ente.getLegalName(), organizationEntity.getLegalName());
		assertNull(organizationEntity.getLogo());
		assertNull(organizationEntity.getLogoMiniature());
		assertEquals(ente.getOfficeAt(), organizationEntity.getOfficeAt());
		assertNull(organizationEntity.getOfficeAddress());
		assertNull(organizationEntity.getOfficeAddressDetails());
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
	void UC_5_16_PatchOrganization_AddOfficeAt() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		// Modifico l'utente
		ente.setOfficeAt("Parco della Vittoria 1/A");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.ADD.toString())
				.add("path", "/office_at")
				.add("value", ente.getOfficeAt());

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
		.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
		.andExpect(jsonPath("$.office_at", is(ente.getOfficeAt())))
		.andReturn();

		OrganizationEntity organizationEntity = this.organizationRepository.findById((long) id).get();

		assertEquals(id, organizationEntity.getId());
		assertEquals(ente.getTaxCode(), organizationEntity.getTaxCode());
		assertEquals(ente.getLegalName(), organizationEntity.getLegalName());
		assertNull(organizationEntity.getLogo());
		assertNull(organizationEntity.getLogoMiniature());
		assertEquals(ente.getOfficeAt(), organizationEntity.getOfficeAt());
		assertNull(organizationEntity.getOfficeAddress());
		assertNull(organizationEntity.getOfficeAddressDetails());
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
	void UC_5_17_PatchOrganization_RemoveOfficeAt() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_at", ente.getOfficeAt())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REMOVE.toString())
				.add("path", "/office_at")
				.add("value", "");

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
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
	}

	@Test
	void UC_5_18_PatchOrganization_ReplaceNotExistingOfficeAt() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/office_at")
				.add("value", "Parco della Vittoria 1/A");

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
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
	}
	
	@Test
	void UC_5_19_PatchOrganization_ReplaceOfficeZip() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_zip", ente.getOfficeZip())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.office_zip", is(ente.getOfficeZip())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		// Modifico l'utente
		ente.setOfficeZip("56125");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/office_zip")
				.add("value", ente.getOfficeZip());

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
		.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
		.andExpect(jsonPath("$.office_zip", is(ente.getOfficeZip())))
		.andReturn();

		OrganizationEntity organizationEntity = this.organizationRepository.findById((long) id).get();

		assertEquals(id, organizationEntity.getId());
		assertEquals(ente.getTaxCode(), organizationEntity.getTaxCode());
		assertEquals(ente.getLegalName(), organizationEntity.getLegalName());
		assertNull(organizationEntity.getLogo());
		assertNull(organizationEntity.getLogoMiniature());
		assertEquals(ente.getOfficeZip(), organizationEntity.getOfficeZip());
		assertNull(organizationEntity.getOfficeAddress());
		assertNull(organizationEntity.getOfficeAddressDetails());
		assertNull(organizationEntity.getOfficeEmailAddress());
		assertNull(organizationEntity.getOfficeForeignState());
		assertNull(organizationEntity.getOfficeMunicipality());
		assertNull(organizationEntity.getOfficeMunicipalityDetails());
		assertNull(organizationEntity.getOfficePecAddress());
		assertNull(organizationEntity.getOfficePhoneNumber());
		assertNull(organizationEntity.getOfficeProvince());
		assertNull(organizationEntity.getOfficeAt());
	}

	@Test
	void UC_5_20_PatchOrganization_AddOfficeZip() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		// Modifico l'utente
		ente.setOfficeZip("56125");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.ADD.toString())
				.add("path", "/office_zip")
				.add("value", ente.getOfficeZip());

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
		.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
		.andExpect(jsonPath("$.office_zip", is(ente.getOfficeZip())))
		.andReturn();

		OrganizationEntity organizationEntity = this.organizationRepository.findById((long) id).get();

		assertEquals(id, organizationEntity.getId());
		assertEquals(ente.getTaxCode(), organizationEntity.getTaxCode());
		assertEquals(ente.getLegalName(), organizationEntity.getLegalName());
		assertNull(organizationEntity.getLogo());
		assertNull(organizationEntity.getLogoMiniature());
		assertEquals(ente.getOfficeZip(), organizationEntity.getOfficeZip());
		assertNull(organizationEntity.getOfficeAddress());
		assertNull(organizationEntity.getOfficeAddressDetails());
		assertNull(organizationEntity.getOfficeEmailAddress());
		assertNull(organizationEntity.getOfficeForeignState());
		assertNull(organizationEntity.getOfficeMunicipality());
		assertNull(organizationEntity.getOfficeMunicipalityDetails());
		assertNull(organizationEntity.getOfficePecAddress());
		assertNull(organizationEntity.getOfficePhoneNumber());
		assertNull(organizationEntity.getOfficeProvince());
		assertNull(organizationEntity.getOfficeAt());
	}

	@Test
	void UC_5_21_PatchOrganization_RemoveOfficeZip() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_zip", ente.getOfficeZip())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REMOVE.toString())
				.add("path", "/office_zip")
				.add("value", "");

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
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
	}

	@Test
	void UC_5_22_PatchOrganization_ReplaceNotExistingOfficeZip() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/office_zip")
				.add("value", "56125");

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
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
	}
	
	@Test
	void UC_5_23_PatchOrganization_ReplaceOfficeMunicipality() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_municipality", ente.getOfficeMunicipality())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.office_municipality", is(ente.getOfficeMunicipality())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		// Modifico l'utente
		ente.setOfficeMunicipality("56125");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/office_municipality")
				.add("value", ente.getOfficeMunicipality());

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
		.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
		.andExpect(jsonPath("$.office_municipality", is(ente.getOfficeMunicipality())))
		.andReturn();

		OrganizationEntity organizationEntity = this.organizationRepository.findById((long) id).get();

		assertEquals(id, organizationEntity.getId());
		assertEquals(ente.getTaxCode(), organizationEntity.getTaxCode());
		assertEquals(ente.getLegalName(), organizationEntity.getLegalName());
		assertNull(organizationEntity.getLogo());
		assertNull(organizationEntity.getLogoMiniature());
		assertEquals(ente.getOfficeMunicipality(), organizationEntity.getOfficeMunicipality());
		assertNull(organizationEntity.getOfficeAddress());
		assertNull(organizationEntity.getOfficeAddressDetails());
		assertNull(organizationEntity.getOfficeEmailAddress());
		assertNull(organizationEntity.getOfficeForeignState());
		assertNull(organizationEntity.getOfficeZip());
		assertNull(organizationEntity.getOfficeMunicipalityDetails());
		assertNull(organizationEntity.getOfficePecAddress());
		assertNull(organizationEntity.getOfficePhoneNumber());
		assertNull(organizationEntity.getOfficeProvince());
		assertNull(organizationEntity.getOfficeAt());
	}

	@Test
	void UC_5_24_PatchOrganization_AddOfficeMunicipality() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		// Modifico l'utente
		ente.setOfficeMunicipality("56125");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.ADD.toString())
				.add("path", "/office_municipality")
				.add("value", ente.getOfficeMunicipality());

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
		.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
		.andExpect(jsonPath("$.office_municipality", is(ente.getOfficeMunicipality())))
		.andReturn();

		OrganizationEntity organizationEntity = this.organizationRepository.findById((long) id).get();

		assertEquals(id, organizationEntity.getId());
		assertEquals(ente.getTaxCode(), organizationEntity.getTaxCode());
		assertEquals(ente.getLegalName(), organizationEntity.getLegalName());
		assertNull(organizationEntity.getLogo());
		assertNull(organizationEntity.getLogoMiniature());
		assertEquals(ente.getOfficeMunicipality(), organizationEntity.getOfficeMunicipality());
		assertNull(organizationEntity.getOfficeAddress());
		assertNull(organizationEntity.getOfficeAddressDetails());
		assertNull(organizationEntity.getOfficeEmailAddress());
		assertNull(organizationEntity.getOfficeForeignState());
		assertNull(organizationEntity.getOfficeZip());
		assertNull(organizationEntity.getOfficeMunicipalityDetails());
		assertNull(organizationEntity.getOfficePecAddress());
		assertNull(organizationEntity.getOfficePhoneNumber());
		assertNull(organizationEntity.getOfficeProvince());
		assertNull(organizationEntity.getOfficeAt());
	}

	@Test
	void UC_5_25_PatchOrganization_RemoveOfficeMunicipality() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_municipality", ente.getOfficeMunicipality())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REMOVE.toString())
				.add("path", "/office_municipality")
				.add("value", "");

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
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
	}

	@Test
	void UC_5_26_PatchOrganization_ReplaceNotExistingOfficeMunicipality() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/office_municipality")
				.add("value", "56125");

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
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
	}
	
	@Test
	void UC_5_27_PatchOrganization_ReplaceOfficeMunicipalityDetails() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_municipality_details", ente.getOfficeMunicipalityDetails())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.office_municipality_details", is(ente.getOfficeMunicipalityDetails())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		// Modifico l'utente
		ente.setOfficeMunicipalityDetails("56125");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/office_municipality_details")
				.add("value", ente.getOfficeMunicipalityDetails());

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
		.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
		.andExpect(jsonPath("$.office_municipality_details", is(ente.getOfficeMunicipalityDetails())))
		.andReturn();

		OrganizationEntity organizationEntity = this.organizationRepository.findById((long) id).get();

		assertEquals(id, organizationEntity.getId());
		assertEquals(ente.getTaxCode(), organizationEntity.getTaxCode());
		assertEquals(ente.getLegalName(), organizationEntity.getLegalName());
		assertNull(organizationEntity.getLogo());
		assertNull(organizationEntity.getLogoMiniature());
		assertEquals(ente.getOfficeMunicipalityDetails(), organizationEntity.getOfficeMunicipalityDetails());
		assertNull(organizationEntity.getOfficeAddress());
		assertNull(organizationEntity.getOfficeAddressDetails());
		assertNull(organizationEntity.getOfficeEmailAddress());
		assertNull(organizationEntity.getOfficeForeignState());
		assertNull(organizationEntity.getOfficeZip());
		assertNull(organizationEntity.getOfficeMunicipality());
		assertNull(organizationEntity.getOfficePecAddress());
		assertNull(organizationEntity.getOfficePhoneNumber());
		assertNull(organizationEntity.getOfficeProvince());
		assertNull(organizationEntity.getOfficeAt());
	}

	@Test
	void UC_5_28_PatchOrganization_AddOfficeMunicipalityDetails() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		// Modifico l'utente
		ente.setOfficeMunicipalityDetails("56125");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.ADD.toString())
				.add("path", "/office_municipality_details")
				.add("value", ente.getOfficeMunicipalityDetails());

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
		.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
		.andExpect(jsonPath("$.office_municipality_details", is(ente.getOfficeMunicipalityDetails())))
		.andReturn();

		OrganizationEntity organizationEntity = this.organizationRepository.findById((long) id).get();

		assertEquals(id, organizationEntity.getId());
		assertEquals(ente.getTaxCode(), organizationEntity.getTaxCode());
		assertEquals(ente.getLegalName(), organizationEntity.getLegalName());
		assertNull(organizationEntity.getLogo());
		assertNull(organizationEntity.getLogoMiniature());
		assertEquals(ente.getOfficeMunicipalityDetails(), organizationEntity.getOfficeMunicipalityDetails());
		assertNull(organizationEntity.getOfficeAddress());
		assertNull(organizationEntity.getOfficeAddressDetails());
		assertNull(organizationEntity.getOfficeEmailAddress());
		assertNull(organizationEntity.getOfficeForeignState());
		assertNull(organizationEntity.getOfficeZip());
		assertNull(organizationEntity.getOfficeMunicipality());
		assertNull(organizationEntity.getOfficePecAddress());
		assertNull(organizationEntity.getOfficePhoneNumber());
		assertNull(organizationEntity.getOfficeProvince());
		assertNull(organizationEntity.getOfficeAt());
	}

	@Test
	void UC_5_29_PatchOrganization_RemoveOfficeMunicipalityDetails() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_municipality_details", ente.getOfficeMunicipalityDetails())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REMOVE.toString())
				.add("path", "/office_municipality_details")
				.add("value", "");

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
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
	}

	@Test
	void UC_5_30_PatchOrganization_ReplaceNotExistingOfficeMunicipalityDetails() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/office_municipality_details")
				.add("value", "56125");

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
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
	}
	
	@Test
	void UC_5_31_PatchOrganization_ReplaceOfficeProvince() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_province", ente.getOfficeProvince())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.office_province", is(ente.getOfficeProvince())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		// Modifico l'utente
		ente.setOfficeProvince("PI");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/office_province")
				.add("value", ente.getOfficeProvince());

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
		.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
		.andExpect(jsonPath("$.office_province", is(ente.getOfficeProvince())))
		.andReturn();

		OrganizationEntity organizationEntity = this.organizationRepository.findById((long) id).get();

		assertEquals(id, organizationEntity.getId());
		assertEquals(ente.getTaxCode(), organizationEntity.getTaxCode());
		assertEquals(ente.getLegalName(), organizationEntity.getLegalName());
		assertNull(organizationEntity.getLogo());
		assertNull(organizationEntity.getLogoMiniature());
		assertEquals(ente.getOfficeProvince(), organizationEntity.getOfficeProvince());
		assertNull(organizationEntity.getOfficeAddress());
		assertNull(organizationEntity.getOfficeAddressDetails());
		assertNull(organizationEntity.getOfficeEmailAddress());
		assertNull(organizationEntity.getOfficeForeignState());
		assertNull(organizationEntity.getOfficeZip());
		assertNull(organizationEntity.getOfficeMunicipality());
		assertNull(organizationEntity.getOfficePecAddress());
		assertNull(organizationEntity.getOfficePhoneNumber());
		assertNull(organizationEntity.getOfficeMunicipalityDetails());
		assertNull(organizationEntity.getOfficeAt());
	}

	@Test
	void UC_5_32_PatchOrganization_AddOfficeProvince() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		// Modifico l'utente
		ente.setOfficeProvince("PI");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.ADD.toString())
				.add("path", "/office_province")
				.add("value", ente.getOfficeProvince());

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
		.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
		.andExpect(jsonPath("$.office_province", is(ente.getOfficeProvince())))
		.andReturn();

		OrganizationEntity organizationEntity = this.organizationRepository.findById((long) id).get();

		assertEquals(id, organizationEntity.getId());
		assertEquals(ente.getTaxCode(), organizationEntity.getTaxCode());
		assertEquals(ente.getLegalName(), organizationEntity.getLegalName());
		assertNull(organizationEntity.getLogo());
		assertNull(organizationEntity.getLogoMiniature());
		assertEquals(ente.getOfficeProvince(), organizationEntity.getOfficeProvince());
		assertNull(organizationEntity.getOfficeAddress());
		assertNull(organizationEntity.getOfficeAddressDetails());
		assertNull(organizationEntity.getOfficeEmailAddress());
		assertNull(organizationEntity.getOfficeForeignState());
		assertNull(organizationEntity.getOfficeZip());
		assertNull(organizationEntity.getOfficeMunicipality());
		assertNull(organizationEntity.getOfficePecAddress());
		assertNull(organizationEntity.getOfficePhoneNumber());
		assertNull(organizationEntity.getOfficeMunicipalityDetails());
		assertNull(organizationEntity.getOfficeAt());
	}

	@Test
	void UC_5_33_PatchOrganization_RemoveOfficeProvince() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_province", ente.getOfficeProvince())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REMOVE.toString())
				.add("path", "/office_province")
				.add("value", "");

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
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
	}

	@Test
	void UC_5_34_PatchOrganization_ReplaceNotExistingOfficeProvince() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/office_province")
				.add("value", "PI");

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
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
	}
	
	@Test
	void UC_5_35_PatchOrganization_ReplaceOfficeForeignState() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_foreign_state", ente.getOfficeForeignState())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.office_foreign_state", is(ente.getOfficeForeignState())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		// Modifico l'utente
		ente.setOfficeForeignState("DE");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/office_foreign_state")
				.add("value", ente.getOfficeForeignState());

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
		.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
		.andExpect(jsonPath("$.office_foreign_state", is(ente.getOfficeForeignState())))
		.andReturn();

		OrganizationEntity organizationEntity = this.organizationRepository.findById((long) id).get();

		assertEquals(id, organizationEntity.getId());
		assertEquals(ente.getTaxCode(), organizationEntity.getTaxCode());
		assertEquals(ente.getLegalName(), organizationEntity.getLegalName());
		assertNull(organizationEntity.getLogo());
		assertNull(organizationEntity.getLogoMiniature());
		assertEquals(ente.getOfficeForeignState(), organizationEntity.getOfficeForeignState());
		assertNull(organizationEntity.getOfficeAddress());
		assertNull(organizationEntity.getOfficeAddressDetails());
		assertNull(organizationEntity.getOfficeEmailAddress());
		assertNull(organizationEntity.getOfficeProvince());
		assertNull(organizationEntity.getOfficeZip());
		assertNull(organizationEntity.getOfficeMunicipality());
		assertNull(organizationEntity.getOfficePecAddress());
		assertNull(organizationEntity.getOfficePhoneNumber());
		assertNull(organizationEntity.getOfficeMunicipalityDetails());
		assertNull(organizationEntity.getOfficeAt());
	}

	@Test
	void UC_5_36_PatchOrganization_AddOfficeForeignState() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		// Modifico l'utente
		ente.setOfficeForeignState("DE");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.ADD.toString())
				.add("path", "/office_foreign_state")
				.add("value", ente.getOfficeForeignState());

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
		.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
		.andExpect(jsonPath("$.office_foreign_state", is(ente.getOfficeForeignState())))
		.andReturn();

		OrganizationEntity organizationEntity = this.organizationRepository.findById((long) id).get();

		assertEquals(id, organizationEntity.getId());
		assertEquals(ente.getTaxCode(), organizationEntity.getTaxCode());
		assertEquals(ente.getLegalName(), organizationEntity.getLegalName());
		assertNull(organizationEntity.getLogo());
		assertNull(organizationEntity.getLogoMiniature());
		assertEquals(ente.getOfficeForeignState(), organizationEntity.getOfficeForeignState());
		assertNull(organizationEntity.getOfficeAddress());
		assertNull(organizationEntity.getOfficeAddressDetails());
		assertNull(organizationEntity.getOfficeEmailAddress());
		assertNull(organizationEntity.getOfficeProvince());
		assertNull(organizationEntity.getOfficeZip());
		assertNull(organizationEntity.getOfficeMunicipality());
		assertNull(organizationEntity.getOfficePecAddress());
		assertNull(organizationEntity.getOfficePhoneNumber());
		assertNull(organizationEntity.getOfficeMunicipalityDetails());
		assertNull(organizationEntity.getOfficeAt());
	}

	@Test
	void UC_5_37_PatchOrganization_RemoveOfficeForeignState() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.add("office_foreign_state", ente.getOfficeForeignState())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REMOVE.toString())
				.add("path", "/office_foreign_state")
				.add("value", "");

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
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
	}

	@Test
	void UC_5_38_PatchOrganization_ReplaceNotExistingOfficeForeignState() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();

		String json = Json.createObjectBuilder()
				.add("tax_code", ente.getTaxCode())
				.add("legal_name", ente.getLegalName())
				.build()
				.toString();

		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/organizations")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.legal_name", is(ente.getLegalName())))
				.andExpect(jsonPath("$.tax_code", is(ente.getTaxCode())))
				.andReturn();

		// Modifico l'organization
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/office_foreign_state")
				.add("value", "DE");

		String PatchOrganization = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/organizations/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchOrganization)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
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
	}
}
