package it.govhub.govregistry.api.test.controller.authorization;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

import it.govhub.govregistry.api.Application;
import it.govhub.govregistry.api.test.Costanti;
import it.govhub.govregistry.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.RoleEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.repository.OrganizationRepository;
import it.govhub.govregistry.commons.repository.RoleRepository;
import it.govhub.govregistry.commons.repository.ServiceRepository;
import it.govhub.govregistry.commons.repository.UserRepository;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di cancellazione delle Authorization")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class Authorization_UC_4_DeleteAuthorizationTest {

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
	
	private OrganizationEntity leggiEnteDB(String nome) {
		List<OrganizationEntity> findAll = this.organizationRepository.findAll();
		return findAll.stream().filter(f -> f.getTaxCode().equals(nome)).collect(Collectors.toList()).get(0);
	}
	
	private ServiceEntity leggiServizioDB(String nome) {
		List<ServiceEntity> findAll = this.serviceRepository.findAll();
		return findAll.stream().filter(f -> f.getName().equals(nome)).collect(Collectors.toList()).get(0);
	}
	
	@Test
	void UC_4_01_DeleteAuthorizationOk() throws Exception {
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(30); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect(jsonPath("$.organizations", is(new ArrayList<>())))
				.andExpect(jsonPath("$.services", is(new ArrayList<>())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole = reader.readObject().getInt("id");
		
		result = this.mockMvc.perform(get("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(1, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		assertEquals(idRole, items.getJsonObject(0).getInt("id"));
		assertEquals(ruoloUser.getName(), items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals(0, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(0, items.getJsonObject(0).getJsonArray("services").size());
		
		// Cancellazione Autorizzazione
		this.mockMvc.perform(delete("/authorizations/{id}", idRole)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isOk())
				.andReturn();
		
		// Verifica che la lista autorizzazioni sia vuota
		result = this.mockMvc.perform(get("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();
		
		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(0, items.size());
	}
	
	@Test
	void UC_4_02_DeleteAuthorizationOk_Organization() throws Exception {
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(30); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect(jsonPath("$.organizations[0].tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.services", is(new ArrayList<>())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole = reader.readObject().getInt("id");
		
		result = this.mockMvc.perform(get("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(1, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		assertEquals(idRole, items.getJsonObject(0).getInt("id"));
		assertEquals(ruoloUser.getName(), items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals(1, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(0, items.getJsonObject(0).getJsonArray("services").size());
		assertEquals(ente.getTaxCode(), items.getJsonObject(0).getJsonArray("organizations").getJsonObject(0).getString("tax_code"));
		
		// Cancellazione Autorizzazione
		this.mockMvc.perform(delete("/authorizations/{id}", idRole)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isOk())
				.andReturn();
		
		// Verifica che la lista autorizzazioni sia vuota
		result = this.mockMvc.perform(get("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();
		
		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(0, items.size());
	}
	
	@Test
	void UC_4_03_DeleteAuthorizationOk_Service() throws Exception {
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(30); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect(jsonPath("$.organizations", is(new ArrayList<>())))
				.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole = reader.readObject().getInt("id");
		
		result = this.mockMvc.perform(get("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(1, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		assertEquals(idRole, items.getJsonObject(0).getInt("id"));
		assertEquals(ruoloUser.getName(), items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals(0, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(1, items.getJsonObject(0).getJsonArray("services").size());
		assertEquals(servizio.getName(), items.getJsonObject(0).getJsonArray("services").getJsonObject(0).getString("service_name"));
		// aggiungere check data dopo che si sistema il servizio
		
		// Cancellazione Autorizzazione
		this.mockMvc.perform(delete("/authorizations/{id}", idRole)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isOk())
				.andReturn();
		
		// Verifica che la lista autorizzazioni sia vuota
		result = this.mockMvc.perform(get("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();
		
		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(0, items.size());
	}
	
	@Test
	void UC_4_04_DeleteAuthorizationOk_UserEditor() throws Exception {
		UserEntity user = leggiUtenteDB("user_viewer");
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(30); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaUserEditor())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect(jsonPath("$.organizations", is(new ArrayList<>())))
				.andExpect(jsonPath("$.services", is(new ArrayList<>())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole = reader.readObject().getInt("id");
		
		result = this.mockMvc.perform(get("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());
		
		assertEquals(idRole, items.getJsonObject(0).getInt("id"));
		assertEquals(ruoloUser.getName(), items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals("govhub_users_viewer", items.getJsonObject(1).getJsonObject("role").getString("role_name"));
		assertEquals(0, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(0, items.getJsonObject(0).getJsonArray("services").size());
		
		// Cancellazione Autorizzazione
		this.mockMvc.perform(delete("/authorizations/{id}", idRole)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isOk())
				.andReturn();
		
		// Verifica che la lista autorizzazioni sia vuota
		result = this.mockMvc.perform(get("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();
		
		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(1, page.getInt("total"));
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		assertEquals("govhub_users_viewer", items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals(0, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(0, items.getJsonObject(0).getJsonArray("services").size());
	}
	
	@Test
	/*
	 * Con l'admin assegno all'utenza SNakamoto la possibilita' di editare i ruoli per l'ente[3]
	 * Con l'utenza SNakamoto edito i ruoli dell'utenza user_viewer assegnado il ruolo user_viewer per l'ente[3].
	 * */
	void UC_4_05_DeleteAuthorizationOk_UserEditor_Organization() throws Exception {
		// Assegno all'utenza SNakamoto la possibilita' di editare i ruoli
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_users_editor");
		
		OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(30); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_users_editor")))
				.andExpect(jsonPath("$.organizations[0].tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.services", is(new ArrayList<>())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		user = leggiUtenteDB("user_viewer");
		
		ruoloUser = leggiRuoloDB("govhub_user");
		
		json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		result = this.mockMvc.perform(post("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect(jsonPath("$.organizations[0].tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.services", is(new ArrayList<>())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole = reader.readObject().getInt("id");
		
		result = this.mockMvc.perform(get("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());
		
		assertEquals(idRole, items.getJsonObject(0).getInt("id"));
		assertEquals(ruoloUser.getName(), items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals("govhub_users_viewer", items.getJsonObject(1).getJsonObject("role").getString("role_name"));
		assertEquals(1, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(0, items.getJsonObject(0).getJsonArray("services").size());
		assertEquals(ente.getTaxCode(), items.getJsonObject(0).getJsonArray("organizations").getJsonObject(0).getString("tax_code"));
		
		// Cancellazione Autorizzazione
		this.mockMvc.perform(delete("/authorizations/{id}", idRole)
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isOk())
				.andReturn();
		
		// Verifica che la lista autorizzazioni sia vuota
		result = this.mockMvc.perform(get("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();
		
		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(1, page.getInt("total"));
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		assertEquals("govhub_users_viewer", items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals(0, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(0, items.getJsonObject(0).getJsonArray("services").size());
	}
	
	// TODO: Riabilita test
	//@Test
	//@Transactional
	/*
	 * Con l'admin assegno all'utenza SNakamoto la possibilita' di editare i ruoli per tutti gli enti
	 * Con l'utenza SNakamoto edito i ruoli dell'utenza user_viewer assegnado il ruolo user_viewer per l'ente[3].
	 * */
	void UC_4_06_DeleteAuthorizationOk_UserEditor_Organization_Ente3() throws Exception {
		// Assegno all'utenza SNakamoto la possibilita' di editare i ruoli
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_users_editor");
		
		OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(30); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_users_editor")))
				.andExpect(jsonPath("$.organizations", is(new ArrayList<>())))
				.andExpect(jsonPath("$.services", is(new ArrayList<>())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		user = leggiUtenteDB("user_viewer");
		
		ruoloUser = leggiRuoloDB("govhub_user");
		
		json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		result = this.mockMvc.perform(post("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect(jsonPath("$.organizations[0].tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.services", is(new ArrayList<>())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole = reader.readObject().getInt("id");
		
		result = this.mockMvc.perform(get("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());
		
		assertEquals(idRole, items.getJsonObject(0).getInt("id"));
		assertEquals(ruoloUser.getName(), items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals("govhub_users_viewer", items.getJsonObject(1).getJsonObject("role").getString("role_name"));
		assertEquals(1, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(0, items.getJsonObject(0).getJsonArray("services").size());
		assertEquals(ente.getTaxCode(), items.getJsonObject(0).getJsonArray("organizations").getJsonObject(0).getString("tax_code"));
		
		// Cancellazione Autorizzazione
		this.mockMvc.perform(delete("/authorizations/{id}", idRole)
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isOk())
				.andReturn();
		
		// Verifica che la lista autorizzazioni sia vuota
		result = this.mockMvc.perform(get("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();
		
		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(1, page.getInt("total"));
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		assertEquals("govhub_users_viewer", items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals(0, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(0, items.getJsonObject(0).getJsonArray("services").size());
	}
	
	// TODO: Riabilita test
	//@Test
	//@Transactional
	/*
	 * Con l'admin assegno all'utenza SNakamoto la possibilita' di editare i ruoli per il servizio[2]
	 * Con l'utenza SNakamoto edito i ruoli dell'utenza user_viewer assegnado il ruolo user_viewer per il servizio[2]
	 * */
	void UC_4_07_DeleteAuthorizationOk_UserEditor_Service() throws Exception {
		// Assegno all'utenza SNakamoto la possibilita' di editare i ruoli
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_users_editor");
		
		OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(30); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_users_editor")))
				.andExpect(jsonPath("$.organizations", is(new ArrayList<>())))
				.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		user = leggiUtenteDB("user_viewer");
		
		ruoloUser = leggiRuoloDB("govhub_user");
		
		json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
//				.add("expiration_date", dt.format(now))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		result = this.mockMvc.perform(post("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect(jsonPath("$.organizations", is(new ArrayList<>())))
				.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
//				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole = reader.readObject().getInt("id");
		
		result = this.mockMvc.perform(get("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());
		
		assertEquals(idRole, items.getJsonObject(0).getInt("id"));
		assertEquals(ruoloUser.getName(), items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals("govhub_users_viewer", items.getJsonObject(1).getJsonObject("role").getString("role_name"));
		assertEquals(0, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(1, items.getJsonObject(0).getJsonArray("services").size());
		assertEquals(servizio.getName(), items.getJsonObject(0).getJsonArray("services").getJsonObject(0).getString("service_name"));
		
		// Cancellazione Autorizzazione
		this.mockMvc.perform(delete("/authorizations/{id}", idRole)
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isOk())
				.andReturn();
		
		// Verifica che la lista autorizzazioni sia vuota
		result = this.mockMvc.perform(get("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();
		
		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(1, page.getInt("total"));
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		assertEquals("govhub_users_viewer", items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals(0, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(0, items.getJsonObject(0).getJsonArray("services").size());
	}
	
	// TODO Riabilita test	
	//@Test
	//@Transactional
	/*
	 * Con l'admin assegno all'utenza SNakamoto la possibilita' di editare i ruoli per tutti i servizi
	 * Con l'utenza SNakamoto edito i ruoli dell'utenza user_viewer assegnado il ruolo user_viewer per il servizio2.
	 * */
	void UC_4_08_DeleteAuthorizationOk_UserEditor_Service_Servizio2() throws Exception {
		// Assegno all'utenza SNakamoto la possibilita' di editare i ruoli
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_users_editor");
		
		OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(30); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_users_editor")))
				.andExpect(jsonPath("$.organizations", is(new ArrayList<>())))
				.andExpect(jsonPath("$.services", is(new ArrayList<>())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		user = leggiUtenteDB("user_viewer");
		
		ruoloUser = leggiRuoloDB("govhub_user");
		
		json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		result = this.mockMvc.perform(post("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is("govhub_user")))
				.andExpect(jsonPath("$.organizations", is(new ArrayList<>())))
				.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRole = reader.readObject().getInt("id");
		
		result = this.mockMvc.perform(get("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());
		
		assertEquals(idRole, items.getJsonObject(0).getInt("id"));
		assertEquals(ruoloUser.getName(), items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals("govhub_users_viewer", items.getJsonObject(1).getJsonObject("role").getString("role_name"));
		assertEquals(0, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(1, items.getJsonObject(0).getJsonArray("services").size());
		assertEquals(servizio.getName(), items.getJsonObject(0).getJsonArray("services").getJsonObject(0).getString("service_name"));
		
		// Cancellazione Autorizzazione
		this.mockMvc.perform(delete("/authorizations/{id}", idRole)
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isOk())
				.andReturn();
		
		// Verifica che la lista autorizzazioni sia vuota
		result = this.mockMvc.perform(get("/users/{id}/authorizations", user.getId())
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Leggo la lista delle autorizzazioni
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();
		
		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(1, page.getInt("total"));
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		assertEquals("govhub_users_viewer", items.getJsonObject(0).getJsonObject("role").getString("role_name"));
		assertEquals(0, items.getJsonObject(0).getJsonArray("organizations").size());
		assertEquals(0, items.getJsonObject(0).getJsonArray("services").size());
	}
}
