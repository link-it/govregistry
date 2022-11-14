package it.govhub.rest.backoffice.test.controller.user;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

import it.govhub.rest.backoffice.Application;
import it.govhub.rest.backoffice.beans.PatchOp.OpEnum;
import it.govhub.rest.backoffice.test.utils.UserAuthProfilesUtils;



@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di censimento Utenti")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

public class PatchUserTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void UC_1_6_PatchUser_Enabled() throws Exception {

		String createUser = Json.createObjectBuilder()
				.add("enabled", false)
				.add("full_name", "Satoshi Nakamoto")
				.add("principal", "snakamoto")
				.build()
				.toString();

		// Creo un utente
		MvcResult result = this.mockMvc.perform(post("/users")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(createUser)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andReturn();

		// Modifico l'utente
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");


		String patchUser = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/enabled")
				.add("value", true)
				.build()
				.toString();

		this.mockMvc.perform(patch("/users/{id}", id)
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(patchUser)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.enabled", is(true)))
				.andExpect(jsonPath("$.full_name", is("Satoshi Nakamoto")))
				.andExpect(jsonPath("$.principal", is("snakamoto")))
				.andReturn();
	}
}
