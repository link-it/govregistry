package it.govhub.govregistry.api.test.controller.service;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import it.govhub.govregistry.api.repository.ServiceRepository;
import it.govhub.govregistry.api.test.Costanti;
import it.govhub.govregistry.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.ServiceEntity;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di creazione dei Services")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

class Service_UC_1_CreateServiceTest {

	private static final String SERVICES_BASE_PATH = "/v1/services";

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ServiceRepository serviceRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Test
	void UC_1_01_CreateServiceOk() throws Exception {
		ServiceEntity servizio = Costanti.getServizioTest();

		String json = Json.createObjectBuilder()
				.add("service_name", servizio.getName())
				.add("description", servizio.getDescription())
				.build()
				.toString();

		// Creo un service e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(SERVICES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.service_name", is(servizio.getName())))
				.andExpect(jsonPath("$.description", is(servizio.getDescription())))
				.andReturn();
		
		// Leggo l'organization dal servizio e verifico
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		ServiceEntity serviceEntity = this.serviceRepository.findById((long) id).get();
		
		assertEquals(id, serviceEntity.getId());
		assertEquals(servizio.getName(), serviceEntity.getName());
		assertEquals(servizio.getDescription(), serviceEntity.getDescription());
		
	}
}
