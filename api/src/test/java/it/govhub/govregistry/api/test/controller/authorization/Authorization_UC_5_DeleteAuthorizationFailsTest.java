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
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import it.govhub.govregistry.api.Application;
import it.govhub.govregistry.api.repository.OrganizationRepository;
import it.govhub.govregistry.api.repository.ServiceRepository;
import it.govhub.govregistry.api.repository.UserRepository;
import it.govhub.govregistry.api.test.Costanti;
import it.govhub.govregistry.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.RoleEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.repository.ReadRoleRepository;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di cancellazione delle Authorization")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class Authorization_UC_5_DeleteAuthorizationFailsTest {
	
	private static final String AUTHORIZATIONS_BASE_PATH_DETAIL_ID = "/v1/authorizations/{id}";
	
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
	
//	@Test
	/*
	 * Con l'admin assegno all'utenza SNakamoto la possibilita' di editare i ruoli per l'ente[3]
	 * Con l'utenza SNakamoto edito i ruoli dell'utenza user_viewer assegnado il ruolo user_viewer per l'ente[3].
	 * Con l'utenza SNakamato provo a cancellare un'autorizzazione su un ruolo che non puo' gestire
	 * */
	void UC_4_01_DeleteAuthorizationFail_UserEditor_Organization() throws Exception {
		// Assegno all'utenza SNakamoto la possibilita' di editare i ruoli
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_users_editor");
		
		// 1. L'amministratore concede l'autorizzazione a modificare le autorizzazioni all'utenza SNakamoto 
		OffsetDateTime expirationDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(30).toOffsetDateTime();  
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
				.andExpect(jsonPath("$.role.role_name", is("govhub_users_editor")))
				.andExpect(jsonPath("$.organizations[0].tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.services", is(new ArrayList<>())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		// 2. Aggiungo un'autorizzazione per l'utenza user_viewer con l'utenza SNakamoto
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
		result = this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
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
		
		// 2. Aggiungo un'autorizzazione per l'utenza user_viewer con l'utenza amministratore
		RoleEntity ruoloAssegnatoAdmin = leggiRuoloDB("govhub_ruolo_non_assegnabile");
		
		json = Json.createObjectBuilder()
				.add("role", ruoloAssegnatoAdmin.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(expirationDate))
				.build()
				.toString();
		
		// Creo una organization e verifico la risposta
		result = this.mockMvc.perform(post(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.role.role_name", is(ruoloAssegnatoAdmin.getName())))
				.andExpect(jsonPath("$.organizations[0].tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.services", is(new ArrayList<>())))
				.andExpect(jsonPath("$.expiration_date", is(dt.format(expirationDate))))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int idRoleAssegnatoAdmin = reader.readObject().getInt("id");
		
		result = this.mockMvc.perform(get(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
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
		assertEquals(3, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(3, items.size());
		
		JsonObject item0 = items.getJsonObject(0);
		JsonObject item1 = items.getJsonObject(1);
		JsonObject item2 = items.getJsonObject(2);

		assertEquals(idRoleAssegnatoAdmin, item0.getInt("id"));
		assertEquals(idRole, item1.getInt("id"));
		assertEquals(ruoloUser.getName(), item1.getJsonObject("role").getString("role_name"));
		assertEquals("govhub_users_viewer", item2.getJsonObject("role").getString("role_name"));
		
		assertEquals(1, item1.getJsonArray("organizations").size());
		assertEquals(0, item1.getJsonArray("services").size());
		assertEquals(ente.getTaxCode(), item1.getJsonArray("organizations").getJsonObject(0).getString("tax_code"));
		
		// Cancellazione Autorizzazione
		this.mockMvc.perform(delete(AUTHORIZATIONS_BASE_PATH_DETAIL_ID, idRoleAssegnatoAdmin)
				.with(this.userAuthProfilesUtils.utenzaPrincipal(Costanti.PRINCIPAL_SNAKAMOTO))
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
		// Verifica che la lista autorizzazioni non sia stata modificata
		result = this.mockMvc.perform(get(USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
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
		assertEquals(3, page.getInt("total"));
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(3, items.size());
		
		assertEquals(idRoleAssegnatoAdmin, item0.getInt("id"));
		assertEquals(idRole, item1.getInt("id"));
		assertEquals(ruoloUser.getName(), item1.getJsonObject("role").getString("role_name"));
		assertEquals("govhub_users_viewer", item2.getJsonObject("role").getString("role_name"));
	}
	
	@Test
	void UC_4_02_DeleteAuthorizationFail_NotFound() throws Exception {
		int idRole = 10000;
		// Cancellazione Autorizzazione non esistente
		this.mockMvc.perform(delete(AUTHORIZATIONS_BASE_PATH_DETAIL_ID, idRole)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.title", is("Not Found")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
}

