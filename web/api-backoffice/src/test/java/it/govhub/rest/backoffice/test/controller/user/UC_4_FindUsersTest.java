package it.govhub.rest.backoffice.test.controller.user;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import it.govhub.rest.backoffice.Application;
import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.repository.UserRepository;
import it.govhub.rest.backoffice.test.Costanti;
import it.govhub.rest.backoffice.test.utils.UserAuthProfilesUtils;



@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura degli Utenti")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

class UC_4_FindUsersTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserRepository userRepository;
	
	@BeforeEach
	private void caricaUtenti() {
		UserEntity user = Costanti.getUser_Vbuterin();
		this.userRepository.save(user);
		
		UserEntity user2 = Costanti.getUser_Snakamoto();
		this.userRepository.save(user2);
	}
	
	@Test
	void UC_4_01_FindAllOk() throws Exception {
		UserEntity user = Costanti.getUser_Vbuterin();
		UserEntity user2 = Costanti.getUser_Snakamoto();
		
		MvcResult result = this.mockMvc.perform(get("/users")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());
		
		JsonObject item1 = items.getJsonObject(0);
		assertEquals(user.getEnabled(), Boolean.parseBoolean(item1.get("enabled").toString()));
		assertEquals(user.getFullName(), item1.getString("full_name"));
		assertEquals(user.getPrincipal(), item1.getString("principal"));
		
		JsonObject item2 = items.getJsonObject(1);
		assertEquals(user2.getEnabled(), Boolean.parseBoolean(item2.get("enabled").toString()));
		assertEquals(user2.getFullName(), item2.getString("full_name"));
		assertEquals(user2.getPrincipal(), item2.getString("principal"));
	}
	
	@Test
	void UC_4_02_FindAllOk_Limit() throws Exception {
		UserEntity user = Costanti.getUser_Vbuterin();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "3");
		
		MvcResult result = this.mockMvc.perform(get("/users").params(params )
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(3, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		JsonObject item1 = items.getJsonObject(0);
		assertEquals(user.getEnabled(), Boolean.parseBoolean(item1.get("enabled").toString()));
		assertEquals(user.getFullName(), item1.getString("full_name"));
		assertEquals(user.getPrincipal(), item1.getString("principal"));
		
	}
	
	@Test
	void UC_4_03_FindAllOk_InvalidLimit() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "XXX");
		
		this.mockMvc.perform(get("/users").params(params )
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	@Test
	void UC_4_04_FindAllOk_Offset() throws Exception {
		UserEntity user2 = Costanti.getUser_Snakamoto();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "1");
		
		MvcResult result = this.mockMvc.perform(get("/users").params(params )
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(1, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		JsonObject item1 = items.getJsonObject(0);
		assertEquals(user2.getEnabled(), Boolean.parseBoolean(item1.get("enabled").toString()));
		assertEquals(user2.getFullName(), item1.getString("full_name"));
		assertEquals(user2.getPrincipal(), item1.getString("principal"));
		
	}
	
	@Test
	void UC_4_05_FindAllOk_InvalidOffset() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "XXX");
		
		this.mockMvc.perform(get("/users").params(params )
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	@Test
	void UC_4_06_FindAllOk_Q() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_Q, "naka");
		
		MvcResult result = this.mockMvc.perform(get("/users").params(params )
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		JsonObject item1 = items.getJsonObject(0);
		assertEquals(user.getEnabled(), Boolean.parseBoolean(item1.get("enabled").toString()));
		assertEquals(user.getFullName(), item1.getString("full_name"));
		assertEquals(user.getPrincipal(), item1.getString("principal"));
		
	}
	
	@Test
	void UC_4_07_FindAllOk_Enabled() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_ENABLED, "true");
		
		MvcResult result = this.mockMvc.perform(get("/users").params(params )
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(0, items.size());
		
	}
	
	@Test
	void UC_4_08_FindAllOk_InvalidEnabled() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_ENABLED, "XXX");
		
		this.mockMvc.perform(get("/users").params(params )
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	@Test
	void UC_4_09_FindAllOk_SortFullname() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();
		UserEntity user2 = Costanti.getUser_Vbuterin();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "full_name");
		
		MvcResult result = this.mockMvc.perform(get("/users").params(params )
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());
		
		JsonObject item1 = items.getJsonObject(0);
		assertEquals(user.getEnabled(), Boolean.parseBoolean(item1.get("enabled").toString()));
		assertEquals(user.getFullName(), item1.getString("full_name"));
		assertEquals(user.getPrincipal(), item1.getString("principal"));
		
		JsonObject item2 = items.getJsonObject(1);
		assertEquals(user2.getEnabled(), Boolean.parseBoolean(item2.get("enabled").toString()));
		assertEquals(user2.getFullName(), item2.getString("full_name"));
		assertEquals(user2.getPrincipal(), item2.getString("principal"));
	}
	
	@Test
	void UC_4_10_FindAllOk_SortId() throws Exception {
		UserEntity user = Costanti.getUser_Vbuterin();
		UserEntity user2 = Costanti.getUser_Snakamoto();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "id");
		
		MvcResult result = this.mockMvc.perform(get("/users").params(params )
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());
		
		JsonObject item1 = items.getJsonObject(0);
		assertEquals(user.getEnabled(), Boolean.parseBoolean(item1.get("enabled").toString()));
		assertEquals(user.getFullName(), item1.getString("full_name"));
		assertEquals(user.getPrincipal(), item1.getString("principal"));
		
		JsonObject item2 = items.getJsonObject(1);
		assertEquals(user2.getEnabled(), Boolean.parseBoolean(item2.get("enabled").toString()));
		assertEquals(user2.getFullName(), item2.getString("full_name"));
		assertEquals(user2.getPrincipal(), item2.getString("principal"));
	}
}
