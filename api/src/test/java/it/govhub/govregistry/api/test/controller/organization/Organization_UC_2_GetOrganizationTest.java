package it.govhub.govregistry.api.test.controller.organization;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.jupiter.api.BeforeEach;
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
import it.govhub.govregistry.api.test.Costanti;
import it.govhub.govregistry.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.repository.OrganizationRepository;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura delle Organizations")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

class Organization_UC_2_GetOrganizationTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@BeforeEach
	private void caricaUtenti() {
		OrganizationEntity ente = Costanti.getEnteCreditore3();
		this.organizationRepository.save(ente);
	}
	
	@Test
	void UC_2_01_GetOrganizationOk() throws Exception {
		OrganizationEntity ente = Costanti.getEnteCreditore3();
		
		MvcResult result = this.mockMvc.perform(get("/organizations/")
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(3, items.size());
		
		JsonObject item1 = items.getJsonObject(0); 
		int idUser1 = item1.getInt("id");
		
		result = this.mockMvc.perform(get("/organizations/{id}",idUser1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject item = reader.readObject();
		
		assertEquals(ente.getLegalName(), item.getString("legal_name"));
		assertEquals(ente.getOfficeAddress(), item.getString("office_address"));
		assertEquals(ente.getOfficeAddressDetails(), item.getString("office_address_details"));
		assertEquals(ente.getOfficeAt(), item.getString("office_at"));
		assertEquals(ente.getOfficeEmailAddress(), item.getString("office_email_address"));
		assertEquals(ente.getOfficeForeignState(), item.getString("office_foreign_state"));
		assertEquals(ente.getOfficeMunicipality(), item.getString("office_municipality"));
		assertEquals(ente.getOfficeMunicipalityDetails(), item.getString("office_municipality_details"));
		assertEquals(ente.getOfficePecAddress(), item.getString("office_pec_address"));
		assertEquals(ente.getOfficePhoneNumber(), item.getString("office_phone_number"));
		assertEquals(ente.getOfficeProvince(), item.getString("office_province"));
		assertEquals(ente.getOfficeZip(), item.getString("office_zip"));
		assertEquals(ente.getTaxCode(), item.getString("tax_code"));
		
	}
	
	@Test
	void UC_2_02_GetOrganization_NotFound() throws Exception {
		int idUser1 = 10000;
		
		this.mockMvc.perform(get("/organizations/{id}",idUser1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.title", is("Not Found")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	@Test	
	void UC_2_03_GetOrganization_InvalidId() throws Exception {
		String idUser1 = "XXX";
		
		this.mockMvc.perform(get("/organizations/{id}",idUser1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
}
