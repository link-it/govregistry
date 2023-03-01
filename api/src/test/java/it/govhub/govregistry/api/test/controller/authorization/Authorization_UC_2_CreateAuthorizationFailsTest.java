package it.govhub.govregistry.api.test.controller.authorization;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;

import it.govhub.govregistry.api.Application;
import it.govhub.govregistry.api.repository.OrganizationRepository;
import it.govhub.govregistry.api.repository.ServiceRepository;
import it.govhub.govregistry.api.repository.UserRepository;
import it.govhub.govregistry.api.test.Costanti;
import it.govhub.govregistry.api.test.utils.Matchers;
import it.govhub.govregistry.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.RoleEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.repository.ReadRoleRepository;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di creazione delle Authorization")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

class Authorization_UC_2_CreateAuthorizationFailsTest {
	
	private static final String USERS_ID_AUTHORIZATIONS_BASE_PATH = "/v1/users/{id}/authorizations";

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Autowired
	private ServiceRepository serviceRepository;
	
	@Autowired
	public ReadRoleRepository roleRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Autowired
	private UserRepository userRepository;
	
	private DateTimeFormatter dt = DateTimeFormatter.ISO_DATE_TIME;
	
	@Value("${govhub.time-zone:Europe/Rome}")
	private String timeZone;
	
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
	
	private OrganizationEntity leggiEnteDB(String nome) {
		List<OrganizationEntity> findAll = this.organizationRepository.findAll();
		return findAll.stream().filter(f -> f.getTaxCode().equals(nome)).collect(Collectors.toList()).get(0);
	}
	
	private ServiceEntity leggiServizioDB(String nome) {
		List<ServiceEntity> findAll = this.serviceRepository.findAll();
		return findAll.stream().filter(f -> f.getName().equals(nome)).collect(Collectors.toList()).get(0);
	}

	@Test
	void UC_2_01_CreateAuthorizationFail_MissingRole() throws Exception {
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
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
	void UC_2_02_CreateAuthorizationFail_UserNotFound() throws Exception {
		int idUser1 = 10000;
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, idUser1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.title", is("Not Found")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_2_03_CreateAuthorizationFail_RoleNotFound() throws Exception {
		int idUser1 = 10000;
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", idUser1)
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
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
	void UC_2_04_CreateAuthorizationFail_OrganizationNotFound() throws Exception {
		int idUser1 = 10000;
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(idUser1))
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
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
	void UC_2_05_CreateAuthorizationFail_ServiceNotFound() throws Exception {
		int idUser1 = 10000;
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(idUser1))
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
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
	void UC_2_06_CreateAuthorizationFail_Unauthorized() throws Exception {
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
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
	
	@Test
	void UC_2_07_CreateAuthorizationFail_ExpirationDate_WrongFormat() throws Exception {
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		DateTimeFormatter dt = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault());
				
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
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
	void UC_2_08_CreateAuthorizationFail_UserEditor() throws Exception {
		UserEntity user = leggiUtenteDB("user_viewer");
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_ruolo_non_assegnabile");
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaUserEditor())
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
	
	@Test
	@Transactional
	/*
	 * Con l'admin assegno all'utenza SNakamoto la possibilita' di editare i ruoli per l'ente[3]
	 * Con l'utenza SNakamoto edito i ruoli dell'utenza user_viewer assegnado il ruolo user_viewer per tutti gli enti, non e' consentito per la restrizione sull'ente.
	 * */
	void UC_2_09_CreateAuthorizationFail_UserEditor_Organization() throws Exception {
		// Assegno all'utenza SNakamoto la possibilita' di editare i ruoli
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_users_editor");
		
//		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder())
//				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_users_editor")))
				.andExpect(jsonPath("$.organizations[0].tax_code", is(ente.getTaxCode())))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("services")))
//				.andExpect(jsonPath("$.expiration_date", is(now)))
				.andReturn();
		
		user = leggiUtenteDB("user_viewer");
		
		ruoloUser = leggiRuoloDB("govhub_user");
		
		json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
//				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
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
	
	@Test
	@Transactional
	/*
	 * Con l'admin assegno all'utenza SNakamoto la possibilita' di editare i ruoli il servizio[2]
	 * Con l'utenza SNakamoto edito i ruoli dell'utenza user_viewer assegnado il ruolo user_viewer per tutti i servizi, non e' consentito per la restrizione sul servizio
	 * */
	void UC_2_10_CreateAuthorizationFail_UserEditor_Service() throws Exception {
		// Assegno all'utenza SNakamoto la possibilita' di editare i ruoli
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_users_editor");
		
//		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
//				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_users_editor")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
				.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
//				.andExpect(jsonPath("$.expiration_date", is(now)))
				.andReturn();
		
		user = leggiUtenteDB("user_viewer");
		
		ruoloUser = leggiRuoloDB("govhub_user");
		
		json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
//				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
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
	
	@Test
	@Transactional
	/*
	 * Con l'admin assegno all'utenza SNakamoto la possibilita' di editare i ruoli con scadenza
	 * Con l'utenza SNakamoto edito i ruoli dell'utenza user_viewer assegnado il ruolo user_viewer senza scadenza, non e' consentito per la restrizione sulle date
	 * */
	void UC_2_11_CreateAuthorizationFail_UserEditor_ExpirationDate() throws Exception {
		// Assegno all'utenza SNakamoto la possibilita' di editare i ruoli
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_users_editor");
		
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_users_editor")))
				.andExpect( jsonPath("$").value(Matchers.hasNullOrEmpty("organizations")))
				.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(now))))
				.andReturn();
		
		user = leggiUtenteDB("user_viewer");
		
		ruoloUser = leggiRuoloDB("govhub_user");
		
		json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
//				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
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
}

