package it.govhub.govregistry.api.test.controller.authorization;


import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonReader;
import javax.transaction.Transactional;

import org.hibernate.Hibernate;
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
import it.govhub.govregistry.commons.entity.RoleAuthorizationEntity;
import it.govhub.govregistry.commons.entity.RoleEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.repository.OrganizationRepository;
import it.govhub.govregistry.readops.api.repository.RoleAuthorizationRepository;
import it.govhub.govregistry.readops.api.repository.RoleRepository;
import it.govhub.govregistry.readops.api.repository.ServiceRepository;
import it.govhub.govregistry.readops.api.repository.UserRepository;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di creazione delle Authorization")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

class Authorization_UC_1_CreateAuthorizationTest {
	
	private static final String USERS_ID_AUTHORIZATIONS_BASE_PATH = "/v1/users/{id}/authorizations";

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private RoleAuthorizationRepository authRepository;
	
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
	
	private OrganizationEntity leggiEnteDB(String nome) {
		List<OrganizationEntity> findAll = this.organizationRepository.findAll();
		return findAll.stream().filter(f -> f.getTaxCode().equals(nome)).collect(Collectors.toList()).get(0);
	}
	
	private ServiceEntity leggiServizioDB(String nome) {
		List<ServiceEntity> findAll = this.serviceRepository.findAll();
		return findAll.stream().filter(f -> f.getName().equals(nome)).collect(Collectors.toList()).get(0);
	}
	
	@Transactional
	private void verificaRegola(int idRegola, OffsetDateTime expirationDate, String nomeRuolo, List<Object> organizations, List<Object> services) {
		RoleAuthorizationEntity roleAuthorizationEntity = this.authRepository.findById((long) idRegola).get();
		
		// Identificativo
		assertEquals(idRegola, roleAuthorizationEntity.getId());
		
		// Nome Ruolo
		assertEquals(nomeRuolo, roleAuthorizationEntity.getRole().getName());
		
		// ExpirationDate
		if(expirationDate == null) {
			assertNull(roleAuthorizationEntity.getExpirationDate());
		} else {
//			assertEquals(expirationDate, roleAuthorizationEntity.getExpirationDate());
		}
		
		Hibernate.initialize(roleAuthorizationEntity.getOrganizations());
		
		// Organizations
		if(organizations == null || organizations.size() == 0) {
			assertEquals(0, roleAuthorizationEntity.getOrganizations().size());
		} else {
			assertEquals(organizations.size(), roleAuthorizationEntity.getOrganizations().size());
			// Aggiungere altre casistiche se servono
			assertEquals(organizations.get(0).toString(), roleAuthorizationEntity.getOrganizations().toArray(new OrganizationEntity[1])[0].getTaxCode());
		}
		
		// Services
		if(services == null || services.size() == 0) {
			assertEquals(0, roleAuthorizationEntity.getServices().size());
		} else {
			assertEquals(services.size(), roleAuthorizationEntity.getServices().size());
			// Aggiungere altre casistiche se servono
			assertEquals(services.get(0).toString(), roleAuthorizationEntity.getServices().toArray(new ServiceEntity[1])[0].getName());
		}
	}

	@Test
	void UC_1_01_CreateAuthorizationOk() throws Exception {
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
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
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
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		verificaRegola(id, expirationDate, ruoloDaAssegnare, null, null);
	}
	
	@Test
	void UC_1_02_CreateAuthorizationOk_Organization() throws Exception {
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		String ruoloDaAssegnare = "govhub_user";
		RoleEntity ruoloUser = leggiRuoloDB(ruoloDaAssegnare);
		
		OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(30); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is(ruoloDaAssegnare)))
				.andExpect(jsonPath("$.organizations[0].tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.services", is(new ArrayList<>())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		verificaRegola(id, expirationDate, ruoloDaAssegnare, Arrays.asList(ente.getTaxCode()), null);
	}
	
	@Test
	void UC_1_03_CreateAuthorizationOk_Service() throws Exception {
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		
		String ruoloDaAssegnare = "govhub_user";
		RoleEntity ruoloUser = leggiRuoloDB(ruoloDaAssegnare);
		
		OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(30); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is(ruoloDaAssegnare)))
				.andExpect(jsonPath("$.organizations", is(new ArrayList<>())))
				.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		verificaRegola(id, expirationDate, ruoloDaAssegnare, null, Arrays.asList(servizio.getName()));
		
	}
	
	@Test
	void UC_1_04_CreateAuthorizationOk_OrganizationService() throws Exception {
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		
		String ruoloDaAssegnare = "govhub_user";
		RoleEntity ruoloUser = leggiRuoloDB(ruoloDaAssegnare);
		
		OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(30); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is(ruoloDaAssegnare)))
				.andExpect(jsonPath("$.organizations[0].tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		verificaRegola(id, expirationDate, ruoloDaAssegnare, Arrays.asList(ente.getTaxCode()), Arrays.asList(servizio.getName()));
		
	}
	
	@Test
	void UC_1_05_CreateAuthorizationOk_NoOrganizationService() throws Exception {
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		String ruoloDaAssegnare = "govhub_user";
		RoleEntity ruoloUser = leggiRuoloDB(ruoloDaAssegnare);
		
		OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(30); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
//				.add("organizations", Json.createArrayBuilder())
//				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
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
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		verificaRegola(id, expirationDate, ruoloDaAssegnare, null, null);
	}
	
	@Test
	void UC_1_06_CreateAuthorizationOk_UserEditor() throws Exception {
		UserEntity user = leggiUtenteDB("user_viewer");
		
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
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
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
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		verificaRegola(id, expirationDate, ruoloDaAssegnare, null, null);
		
	}
	
	@Test
	/*
	 * Con l'admin assegno all'utenza SNakamoto la possibilita' di editare i ruoli per l'ente[3]
	 * Con l'utenza SNakamoto edito i ruoli dell'utenza user_viewer assegnado il ruolo user_viewer per l'ente[3].
	 * */
	void UC_1_07_CreateAuthorizationOk_UserEditor_Organization() throws Exception {
		// Assegno all'utenza SNakamoto la possibilita' di editare i ruoli
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		String ruoloEditor = "govhub_users_editor";
		RoleEntity ruoloUser = leggiRuoloDB(ruoloEditor);
		
		OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(30); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is(ruoloEditor)))
				.andExpect(jsonPath("$.organizations[0].tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.services", is(new ArrayList<>())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		user = leggiUtenteDB("user_viewer");
		
		String ruoloDaAssegnare = "govhub_user";
		ruoloUser = leggiRuoloDB(ruoloDaAssegnare);
		
		json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		result = this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is(ruoloDaAssegnare)))
				.andExpect(jsonPath("$.organizations[0].tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.services", is(new ArrayList<>())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		verificaRegola(id, expirationDate, ruoloDaAssegnare, Arrays.asList(ente.getTaxCode()), null);
	}
	
	@Test
	/*
	 * Con l'admin assegno all'utenza SNakamoto la possibilita' di editare i ruoli per tutti gli enti
	 * Con l'utenza SNakamoto edito i ruoli dell'utenza user_viewer assegnado il ruolo user_viewer per l'ente[3].
	 * */
	void UC_1_08_CreateAuthorizationOk_UserEditor_Organization_Ente3() throws Exception {
		// Assegno all'utenza SNakamoto la possibilita' di editare i ruoli
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		String ruoloEditor = "govhub_users_editor";
		RoleEntity ruoloUser = leggiRuoloDB(ruoloEditor);
		
		OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(30); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is(ruoloEditor)))
				.andExpect(jsonPath("$.organizations", is(new ArrayList<>())))
				.andExpect(jsonPath("$.services", is(new ArrayList<>())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		user = leggiUtenteDB("user_viewer");
		
		String ruoloDaAssegnare = "govhub_user";
		ruoloUser = leggiRuoloDB(ruoloDaAssegnare);
		
		json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		result = this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is(ruoloDaAssegnare)))
				.andExpect(jsonPath("$.organizations[0].tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.services", is(new ArrayList<>())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		verificaRegola(id, expirationDate, ruoloDaAssegnare, Arrays.asList(ente.getTaxCode()), null);
		
	}
	
	@Test
	/*
	 * Con l'admin assegno all'utenza SNakamoto la possibilita' di editare i ruoli per il servizio[2]
	 * Con l'utenza SNakamoto edito i ruoli dell'utenza user_viewer assegnado il ruolo user_viewer per il servizio[2]
	 * */
	void UC_1_09_CreateAuthorizationOk_UserEditor_Service() throws Exception {
		// Assegno all'utenza SNakamoto la possibilita' di editare i ruoli
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		String ruoloEditor = "govhub_users_editor";
		RoleEntity ruoloUser = leggiRuoloDB(ruoloEditor);
		
		OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(30); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is(ruoloEditor)))
				.andExpect(jsonPath("$.organizations", is(new ArrayList<>())))
				.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		user = leggiUtenteDB("user_viewer");
		
		String ruoloDaAssegnare = "govhub_user";
		ruoloUser = leggiRuoloDB(ruoloDaAssegnare);
		
		json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		result = this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is(ruoloDaAssegnare)))
				.andExpect(jsonPath("$.organizations", is(new ArrayList<>())))
				.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		verificaRegola(id, expirationDate, ruoloDaAssegnare, null, Arrays.asList(servizio.getName()));
		
	}
	
	@Test
	/*
	 * Con l'admin assegno all'utenza SNakamoto la possibilita' di editare i ruoli per tutti i servizi
	 * Con l'utenza SNakamoto edito i ruoli dell'utenza user_viewer assegnado il ruolo user_viewer per il servizio2.
	 * */
	void UC_1_10_CreateAuthorizationOk_UserEditor_Service_Servizio2() throws Exception {
		// Assegno all'utenza SNakamoto la possibilita' di editare i ruoli
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		String ruoloEditor = "govhub_users_editor";
		RoleEntity ruoloUser = leggiRuoloDB(ruoloEditor);
		
		OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(30); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())	
				.andExpect(jsonPath("$.role.role_name", is(ruoloEditor)))
				.andExpect(jsonPath("$.organizations", is(new ArrayList<>())))
				.andExpect(jsonPath("$.services", is(new ArrayList<>())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		user = leggiUtenteDB("user_viewer");
		
		String ruoloDaAssegnare = "govhub_user";
		ruoloUser = leggiRuoloDB(ruoloDaAssegnare);
		
		json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		result = this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is(ruoloDaAssegnare)))
				.andExpect(jsonPath("$.organizations", is(new ArrayList<>())))
				.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		verificaRegola(id, expirationDate, ruoloDaAssegnare, null, Arrays.asList(servizio.getName()));
	}
}

