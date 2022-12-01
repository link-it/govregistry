package it.govhub.rest.backoffice.test.controller.user;

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

import it.govhub.rest.backoffice.Application;
import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.test.Costanti;
import it.govhub.rest.backoffice.test.utils.UserAuthProfilesUtils;



@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di censimento Utenti")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

class User_UC_2_CreateUserFailsTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
		
	@Test
	void UC_2_01_CreateUserFail_MissingPrincipal() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();

		String json = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.build()
				.toString();

		// Creo un utente e verifico la risposta
		this.mockMvc.perform(post("/users")
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
	void UC_2_02_CreateUserFail_MissingFullname() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();

		String json = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("principal", user.getPrincipal())
				.build()
				.toString();

		// Creo un utente e verifico la risposta
		this.mockMvc.perform(post("/users")
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
	void UC_2_03_CreateUserFail_MissingEnabled() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();
		
		String json = Json.createObjectBuilder()
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.build()
				.toString();

		// Creo un utente e verifico la risposta
		this.mockMvc.perform(post("/users")
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
	void UC_2_04_CreateUserFail_EnabledAsString() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();
		
		String json = Json.createObjectBuilder()
				.add("enabled", Costanti.PRINCIPAL_VBUTERIN)
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.build()
				.toString();

		// Creo un utente e verifico la risposta
		this.mockMvc.perform(post("/users")
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
	void UC_2_05_CreateUserFail_FullnameTooLong() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();
		user.setFullName(Costanti.STRING_256);
		
		String json = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.build()
				.toString();

		// Creo un utente e verifico la risposta
		this.mockMvc.perform(post("/users")
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
	void UC_2_06_CreateUserFail_PrincipalTooLong() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();
		user.setPrincipal(Costanti.STRING_256);
		
		String json = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.build()
				.toString();

		// Creo un utente e verifico la risposta
		this.mockMvc.perform(post("/users")
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
	void UC_2_07_CreateUserFail_InvalidPrincipal() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();
		user.setPrincipal(Costanti.FULL_NAME_VITALIY_BUTERIN);
		
		String json = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.build()
				.toString();

		// Creo un utente e verifico la risposta
		this.mockMvc.perform(post("/users")
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
	void UC_2_08_CreateUserFail_EmailTooLong() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();
		user.setEmail(Costanti.STRING_256);
		
		String json = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.add("email", user.getEmail())
				.build()
				.toString();

		// Creo un utente e verifico la risposta
		this.mockMvc.perform(post("/users")
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
	void UC_2_09_CreateUserFail_InvalidEmail() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();
		user.setEmail(Costanti.WRONG_EMAIL);
		
		String json = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.add("email", user.getEmail())
				.build()
				.toString();

		// Creo un utente e verifico la risposta
		this.mockMvc.perform(post("/users")
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
	void UC_2_10_CreateUserFail_DuplicateUser() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();
		
		String json = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.add("email", user.getEmail())
				.build()
				.toString();
		
		// Creazione OK
		this.mockMvc.perform(post("/users")
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.enabled", is(user.getEnabled())))
				.andExpect(jsonPath("$.full_name", is(user.getFullName())))
				.andExpect(jsonPath("$.principal", is(user.getPrincipal())))
				.andReturn();

		// Creazione Utente gia' presente.
		this.mockMvc.perform(post("/users")
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
	
	//@Test
	void UC_2_11_CreateUserFail_MissingCsrf() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();

		String json = Json.createObjectBuilder()
				.add("enabled", user.getEnabled())
				.add("full_name", user.getFullName())
				.add("principal", user.getPrincipal())
				.build()
				.toString();

		// Creo un utente e verifico la risposta
		this.mockMvc.perform(post("/users")
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status", is(403)))
				.andExpect(jsonPath("$.title", is("Forbidden")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
}
