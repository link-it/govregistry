package it.govhub.govregistry.application.test.controller.authorization;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
import javax.transaction.Transactional;

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

import it.govhub.govregistry.api.entity.OrganizationEntity;
import it.govhub.govregistry.api.entity.RoleAuthorizationEntity;
import it.govhub.govregistry.api.entity.RoleEntity;
import it.govhub.govregistry.api.entity.ServiceEntity;
import it.govhub.govregistry.api.entity.UserEntity;
import it.govhub.govregistry.api.repository.OrganizationRepository;
import it.govhub.govregistry.api.repository.RoleAuthorizationRepository;
import it.govhub.govregistry.api.repository.RoleRepository;
import it.govhub.govregistry.api.repository.ServiceRepository;
import it.govhub.govregistry.api.repository.UserRepository;
import it.govhub.govregistry.application.Application;
import it.govhub.govregistry.application.test.Costanti;
import it.govhub.govregistry.application.test.utils.UserAuthProfilesUtils;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di creazione delle Authorization")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

public class Authorization_UC_1_CreateAuthorizationTest {

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
	private void configurazDB() {
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
	@Transactional
	void UC_1_01_CreateAuthorizationOk() throws Exception {
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime now = OffsetDateTime.now(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
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
//				.andExpect(jsonPath("$.expiration_date", is(now)))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		RoleAuthorizationEntity roleAuthorizationEntity = this.authRepository.findById((long) id).get();
		
		assertEquals(id, roleAuthorizationEntity.getId());
		assertEquals("govhub_user", roleAuthorizationEntity.getRole().getName());
		assertEquals(0, roleAuthorizationEntity.getOrganizations().size());
		assertEquals(0, roleAuthorizationEntity.getServices().size());
//		assertEquals(now,roleAuthorizationEntity.getExpirationDate());
	}
	
	@Test
	@Transactional
	void UC_1_02_CreateAuthorizationOk_Organization() throws Exception {
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime now = OffsetDateTime.now(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
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
//				.andExpect(jsonPath("$.expiration_date", is(now)))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		RoleAuthorizationEntity roleAuthorizationEntity = this.authRepository.findById((long) id).get();
		
		assertEquals(id, roleAuthorizationEntity.getId());
		assertEquals("govhub_user", roleAuthorizationEntity.getRole().getName());
		assertEquals(1, roleAuthorizationEntity.getOrganizations().size());
		assertEquals(0, roleAuthorizationEntity.getServices().size());
		assertEquals(ente.getTaxCode(), roleAuthorizationEntity.getOrganizations().toArray(new OrganizationEntity[1])[0].getTaxCode());
//		assertEquals(now,roleAuthorizationEntity.getExpirationDate());
	}
	
	@Test
	@Transactional
	void UC_1_03_CreateAuthorizationOk_Service() throws Exception {
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime now = OffsetDateTime.now(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder())
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
				.add("expiration_date", dt.format(now))
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
//				.andExpect(jsonPath("$.expiration_date", is(now)))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		RoleAuthorizationEntity roleAuthorizationEntity = this.authRepository.findById((long) id).get();
		
		assertEquals(id, roleAuthorizationEntity.getId());
		assertEquals("govhub_user", roleAuthorizationEntity.getRole().getName());
		assertEquals(0, roleAuthorizationEntity.getOrganizations().size());
		assertEquals(1, roleAuthorizationEntity.getServices().size());
		assertEquals(servizio.getName(), roleAuthorizationEntity.getServices().toArray(new ServiceEntity[1])[0].getName());
//		assertEquals(now,roleAuthorizationEntity.getExpirationDate());
	}
	
	@Test
	@Transactional
	void UC_1_04_CreateAuthorizationOk_OrganizationService() throws Exception {
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime now = OffsetDateTime.now(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
				.add("organizations", Json.createArrayBuilder().add(ente.getId()))
				.add("services", Json.createArrayBuilder().add(servizio.getId()))
				.add("expiration_date", dt.format(now))
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
				.andExpect(jsonPath("$.services[0].service_name", is(servizio.getName())))
//				.andExpect(jsonPath("$.expiration_date", is(now)))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		RoleAuthorizationEntity roleAuthorizationEntity = this.authRepository.findById((long) id).get();
		
		assertEquals(id, roleAuthorizationEntity.getId());
		assertEquals("govhub_user", roleAuthorizationEntity.getRole().getName());
		assertEquals(1, roleAuthorizationEntity.getOrganizations().size());
		assertEquals(1, roleAuthorizationEntity.getServices().size());
		assertEquals(ente.getTaxCode(), roleAuthorizationEntity.getOrganizations().toArray(new OrganizationEntity[1])[0].getTaxCode());
		assertEquals(servizio.getName(), roleAuthorizationEntity.getServices().toArray(new ServiceEntity[1])[0].getName());
//		assertEquals(now,roleAuthorizationEntity.getExpirationDate());
	}
	
	@Test
	@Transactional
	void UC_1_05_CreateAuthorizationOk() throws Exception {
		UserEntity user = leggiUtenteDB(Costanti.PRINCIPAL_SNAKAMOTO);
		
		RoleEntity ruoloUser = leggiRuoloDB("govhub_user");
		
		OffsetDateTime now = OffsetDateTime.now(); 
		String json = Json.createObjectBuilder()
				.add("role", ruoloUser.getId())
//				.add("organizations", Json.createArrayBuilder())
//				.add("services", Json.createArrayBuilder())
				.add("expiration_date", dt.format(now))
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
//				.andExpect(jsonPath("$.expiration_date", is(now)))
				.andReturn();
		
		// Leggo l'autorizzazione dal servizio e verifico con i dati presenti sul db
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		RoleAuthorizationEntity roleAuthorizationEntity = this.authRepository.findById((long) id).get();
		
		assertEquals(id, roleAuthorizationEntity.getId());
		assertEquals("govhub_user", roleAuthorizationEntity.getRole().getName());
		assertEquals(0, roleAuthorizationEntity.getOrganizations().size());
		assertEquals(0, roleAuthorizationEntity.getServices().size());
//		assertEquals(now,roleAuthorizationEntity.getExpirationDate());
	}
}
