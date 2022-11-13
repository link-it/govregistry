package it.govhub.rest.backoffice.test.controller.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonReader;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
import it.govhub.rest.backoffice.test.utils.UserAuthProfilesUtils;



@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di censimento Utenti")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

public class CreateUserTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void UC_1_1_CreateUserOk() throws Exception {

		String json = Json.createObjectBuilder()
				.add("enabled", false)
				.add("full_name", "Satoshi Nakamoto")
				.add("principal", "snakamoto")
				.build()
				.toString();

		// Creo un utente e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/users")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.enabled", is(false)))
				.andExpect(jsonPath("$.full_name", is("Satoshi Nakamoto")))
				.andExpect(jsonPath("$.principal", is("snakamoto")))
				.andReturn();
		
		// Leggo l'utente dal servizio e verifico
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		this.mockMvc.perform(get("/users/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.enabled", is(false)))
				.andExpect(jsonPath("$.full_name", is("Satoshi Nakamoto")))
				.andExpect(jsonPath("$.principal", is("snakamoto")))
				.andReturn();
	}
	
	@Test
	public void UC_1_2_CreateUserOk_withEmail() throws Exception {

		String json = Json.createObjectBuilder()
				.add("enabled", false)
				.add("full_name", "Satoshi Nakamoto")
				.add("principal", "snakamoto")
				.build()
				.toString();

		// Creo un utente e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/users")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.email", is("snakamoto@mail.xx")))
				.andExpect(jsonPath("$.enabled", is(false)))
				.andExpect(jsonPath("$.full_name", is("Satoshi Nakamoto")))
				.andExpect(jsonPath("$.principal", is("snakamoto")))
				.andReturn();
		
		// Leggo l'utente dal servizio e verifico
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		this.mockMvc.perform(get("/users/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.email", is("snakamoto@mail.xx")))
				.andExpect(jsonPath("$.enabled", is(false)))
				.andExpect(jsonPath("$.full_name", is("Satoshi Nakamoto")))
				.andExpect(jsonPath("$.principal", is("snakamoto")))
				.andReturn();
	}
	

	
}
