package it.govhub.govregistry.api.test.controller.service;

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
import it.govhub.govregistry.commons.entity.ServiceEntity;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di creazione dei Services")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

class Service_UC_2_CreateServiceFailsTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Test
	void UC_2_01_CreateServiceFail_MissingServiceName() throws Exception {
		ServiceEntity servizio = Costanti.getServizioTest();

		String json = Json.createObjectBuilder()
				.add("description", servizio.getDescription())
				.build()
				.toString();

		// Creo un service e verifico la risposta
		this.mockMvc.perform(post("/services")
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
	void UC_2_02_CreateServiceFail_ServiceNameTooLong() throws Exception {
		ServiceEntity servizio = Costanti.getServizioTest();

		String json = Json.createObjectBuilder()
				.add("service_name", Costanti.STRING_256 + Costanti.STRING_256)
				.add("description", servizio.getDescription())
				.build()
				.toString();

		// Creo un service e verifico la risposta
		this.mockMvc.perform(post("/services")
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
	void UC_2_03_CreateServiceFail_DescriptionTooLong() throws Exception {
		ServiceEntity servizio = Costanti.getServizioTest();
		
		String descriptionSup4096 = Costanti.STRING_256 + Costanti.STRING_256 + Costanti.STRING_256 + Costanti.STRING_256
				+ Costanti.STRING_256 + Costanti.STRING_256 + Costanti.STRING_256 + Costanti.STRING_256 
				+ Costanti.STRING_256 + Costanti.STRING_256 + Costanti.STRING_256 + Costanti.STRING_256
				+ Costanti.STRING_256 + Costanti.STRING_256 + Costanti.STRING_256 + Costanti.STRING_256
				+ "4097";
		String json = Json.createObjectBuilder()
				.add("service_name", servizio.getName())
				.add("description", descriptionSup4096)
				.build()
				.toString();

		// Creo un service e verifico la risposta
		this.mockMvc.perform(post("/services")
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
}
