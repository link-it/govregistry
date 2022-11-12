package it.govhub.rest.backoffice.test.controller.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.Charset;

import javax.json.Json;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govhub.rest.backoffice.Application;
import it.govhub.rest.backoffice.beans.User;
import it.govhub.rest.backoffice.beans.UserCreate;
import it.govhub.rest.backoffice.test.utils.UserAuthProfilesUtils;



@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di censimento Utenti")
public class CrudUserTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("Creazione di un nuovo utente")
	public void creazioneUtente() throws Exception {

		String json = Json.createObjectBuilder()
				.add("email", "snakamoto@mail.xx")
				.add("enabled", false)
				.add("full_name", "Satoshi Nakamoto")
				.add("principal", "snakamoto")
				.build()
				.toString();

		MvcResult result = this.mockMvc.perform(post("/users")
				.with(UserAuthProfilesUtils.utenzaAdmin())
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

	}
}
