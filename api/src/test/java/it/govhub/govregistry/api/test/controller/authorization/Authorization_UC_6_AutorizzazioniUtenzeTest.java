package it.govhub.govregistry.api.test.controller.authorization;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
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
import it.govhub.govregistry.commons.entity.RoleEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.repository.OrganizationRepository;
import it.govhub.govregistry.readops.api.repository.RoleRepository;
import it.govhub.govregistry.readops.api.repository.ServiceRepository;
import it.govhub.govregistry.readops.api.repository.UserRepository;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di controllo autorizzazioni sulle operazioni delle Authorization")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

class Authorization_UC_6_AutorizzazioniUtenzeTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Autowired
	private ServiceRepository serviceRepository;
	
	@Autowired
	public RoleRepository roleRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Autowired
	private UserRepository userRepository;
	
	private DateTimeFormatter dt = DateTimeFormatter.ISO_DATE_TIME;
	
	@BeforeEach
	private void configurazioneDB() {
		UserEntity user = Costanti.getUser_Snakamoto();
		this.userRepository.save(user);
		
		OrganizationEntity ente = Costanti.getEnteCreditore3();
		this.organizationRepository.save(ente);
		
		ServiceEntity servizio = Costanti.getServizioTest();
		this.serviceRepository.save(servizio);
	}
	
	private RoleEntity leggiRuoloDB(String nomeRuolo) {
		List<RoleEntity> findAll = this.roleRepository.findAll();
		return findAll.stream().filter(f -> f.getName().equals(nomeRuolo)).collect(Collectors.toList()).get(0);
	}
	
	private UserEntity leggiUtenteDB(String principal) {
		List<UserEntity> findAll = this.userRepository.findAll();
		return findAll.stream().filter(f -> f.getPrincipal().equals(principal)).collect(Collectors.toList()).get(0);
	}
	
	// 1. CreateAuthorization con utenza non admin con ruolo users_editor: Ok
	@Test
	void UC_6_01_CreateAuthorizationOk_UtenzaConRuolo_GovHub_Users_Editor() throws Exception {
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		String ruoloDaAssegnare = "govhub_user";
		RoleEntity ruoloUser = leggiRuoloDB(ruoloDaAssegnare);
		
		OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(30); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una authorization e verifico la risposta
		this.mockMvc.perform(post("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaUserEditor())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is(ruoloDaAssegnare)))
				.andExpect(jsonPath("$.organizations", is(new ArrayList<>())))
				.andExpect(jsonPath("$.services", is(new ArrayList<>())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
	}
	
	// 2. CreateAuthorization con utenza non admin con ruolo non users_editor: NotAuthorized
	@Test
	void UC_6_02_CreateAuthorizationFail_UtenzaSenzaRuolo_GovHub_Users_Editor() throws Exception {
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		String ruoloDaAssegnare = "govhub_user";
		RoleEntity ruoloUser = leggiRuoloDB(ruoloDaAssegnare);
		
		OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(30); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una authorization e verifico la risposta
		this.mockMvc.perform(post("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaUserViewer())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	// 3. FindAllAuthorizations con utenza non admin con ruolo users_editor o users_viewer: Ok
	@Test
	void UC_6_03_FindAllOk_UtenzaConRuolo_GovHub_Users_Editor() throws Exception {
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		this.mockMvc.perform(get("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaUserEditor())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		this.mockMvc.perform(get("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaUserViewer())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
	}
	
	// 4. FindAllAuthorizations con utenza non admin con ruolo non users_editor o users_viewer: NotAuthorized
	@Test
	void UC_6_04_FindAllFail_UtenzaSenzaRuolo_GovHub_Users_Editor() throws Exception {
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		this.mockMvc.perform(get("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaOspite())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	// 5. DeleteAuthorization con utenza non admin con ruolo users_editor: Ok
	@Test
	void UC_6_05_DeleteAuthorizationOk_UtenzaConRuolo_GovHub_Users_Editor() throws Exception {
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		String ruoloDaAssegnare = "govhub_user";
		RoleEntity ruoloUser = leggiRuoloDB(ruoloDaAssegnare);
		
		OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(30); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una authorization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaUserEditor())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is(ruoloDaAssegnare)))
				.andExpect(jsonPath("$.organizations", is(new ArrayList<>())))
				.andExpect(jsonPath("$.services", is(new ArrayList<>())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole = reader.readObject().getInt("id");
		
		// Cancellazione Autorizzazione
		this.mockMvc.perform(delete("/authorizations/{id}", idRole)
				.with(this.userAuthProfilesUtils.utenzaUserEditor())
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isOk())
				.andReturn();
		
	}
	
	// 6. DeleteAuthorization con utenza non admin con ruolo non users_editor: NotAuthorized
	@Test
	void UC_6_06_DeleteAuthorizationFail_UtenzaSenzaRuolo_GovHub_Users_Editor() throws Exception {
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		String ruoloDaAssegnare = "govhub_user";
		RoleEntity ruoloUser = leggiRuoloDB(ruoloDaAssegnare);
		
		OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(30); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una authorization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaUserEditor())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is(ruoloDaAssegnare)))
				.andExpect(jsonPath("$.organizations", is(new ArrayList<>())))
				.andExpect(jsonPath("$.services", is(new ArrayList<>())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole = reader.readObject().getInt("id");
		
		// Cancellazione Autorizzazione
		this.mockMvc.perform(delete("/authorizations/{id}", idRole)
				.with(this.userAuthProfilesUtils.utenzaUserViewer())
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
}
