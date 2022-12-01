package it.govhub.rest.backoffice.test.controller.system;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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

import it.govhub.rest.backoffice.Application;
import it.govhub.rest.backoffice.config.SecurityConfig;
import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.repository.UserRepository;
import it.govhub.rest.backoffice.test.Costanti;
import it.govhub.rest.backoffice.test.utils.UserAuthProfilesUtils;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura degli Utenti")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class System_UC_1_GetProfileTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@BeforeEach
	private void caricaUtenti() {
		UserEntity user = Costanti.getUser_Vbuterin();
		this.userRepository.save(user);
		
		UserEntity user2 = Costanti.getUser_Snakamoto();
		this.userRepository.save(user2);
	}
	
	@Test
	void UC_1_01_GetProfile_UtenzaAdmin_NotAuthorized() throws Exception {
		this.mockMvc.perform(get("/profile")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	@Test
	void UC_1_02_GetProfile_UtenzaAdmin_Forbidden() throws Exception {
		this.mockMvc.perform(get("/profile")
				.with(user("utenza_non_registrata").password("password"))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status", is(403)))
				.andExpect(jsonPath("$.title", is("Forbidden")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	//@Test
	void UC_1_03_GetProfile_UtenzaAdminOk() throws Exception {
		UserEntity user = Costanti.getUser_Vbuterin();
		
		MvcResult result = this.mockMvc.perform(get("/profile")
				.with(user(user.getPrincipal()).password("password").roles(SecurityConfig.RUOLO_GOVHUB_SYSADMIN))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject item = reader.readObject();
		JsonArray authorizations = item.getJsonArray("authorizations");
		
		assertEquals(user.getEnabled(), Boolean.parseBoolean(item.get("enabled").toString()));
		assertEquals(user.getFullName(), item.getString("full_name"));
		assertEquals(user.getPrincipal(), item.getString("principal"));
		assertEquals(0, authorizations.size());
	}
}
