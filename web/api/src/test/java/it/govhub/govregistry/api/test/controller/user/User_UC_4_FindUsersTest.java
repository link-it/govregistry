package it.govhub.govregistry.api.test.controller.user;

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

import it.govhub.govregistry.api.Application;
import it.govhub.govregistry.api.entity.UserEntity;
import it.govhub.govregistry.api.repository.UserRepository;
import it.govhub.govregistry.api.test.Costanti;
import it.govhub.govregistry.api.test.utils.UserAuthProfilesUtils;



@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura degli Utenti")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

class User_UC_4_FindUsersTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@BeforeEach
	private void caricaUtenti() {
		UserEntity user = Costanti.getUser_Snakamoto();
		this.userRepository.save(user);
		
		UserEntity user2 = Costanti.getUser_Vbuterin();
		this.userRepository.save(user2);
	}
	
	@Test
	void UC_4_01_FindAllOk() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();		
		UserEntity user2 = Costanti.getUser_Vbuterin();
		
		MvcResult result = this.mockMvc.perform(get("/users")
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(8, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(8, items.size());
		
		// ordinamento default ID desc
		
		assertEquals(user2.getPrincipal(), items.getJsonObject(0).getString("principal"));
		assertEquals(user.getPrincipal(), items.getJsonObject(1).getString("principal"));
		assertEquals("org_editor", items.getJsonObject(2).getString("principal"));
		assertEquals("org_viewer", items.getJsonObject(3).getString("principal"));
		assertEquals("user_editor", items.getJsonObject(4).getString("principal"));
		assertEquals("user_viewer", items.getJsonObject(5).getString("principal"));
		assertEquals("ospite", items.getJsonObject(6).getString("principal"));
		assertEquals("amministratore", items.getJsonObject(7).getString("principal"));
	}
	
	@Test
	void UC_4_02_FindAllOk_Limit() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();		
		UserEntity user2 = Costanti.getUser_Vbuterin();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "3");
		
		MvcResult result = this.mockMvc.perform(get("/users").params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(3, page.getInt("limit"));
		assertEquals(8, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(3, items.size());
		
		// ordinamento default ID desc
		
		assertEquals(user2.getPrincipal(), items.getJsonObject(0).getString("principal"));
		assertEquals(user.getPrincipal(), items.getJsonObject(1).getString("principal"));
		assertEquals("org_editor", items.getJsonObject(2).getString("principal"));
		
	}
	
	@Test
	void UC_4_03_FindAllOk_InvalidLimit() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "XXX");
		
		this.mockMvc.perform(get("/users").params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
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
		UserEntity user = Costanti.getUser_Snakamoto();		
		UserEntity user2 = Costanti.getUser_Vbuterin();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "1");
		
		MvcResult result = this.mockMvc.perform(get("/users").params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(8, page.getInt("total"));
		
		// Controlli sugli items
//		JsonArray items = userList.getJsonArray("items");
//		assertEquals(7, items.size());
//		
////		assertEquals("amministratore", items.getJsonObject(0).getString("principal"));
//		assertEquals("ospite", items.getJsonObject(0).getString("principal"));
//		assertEquals("user_viewer", items.getJsonObject(1).getString("principal"));
//		assertEquals("user_editor", items.getJsonObject(2).getString("principal"));
//		assertEquals("org_viewer", items.getJsonObject(3).getString("principal"));
//		assertEquals("org_editor", items.getJsonObject(4).getString("principal"));
//		assertEquals(user.getPrincipal(), items.getJsonObject(5).getString("principal"));
//		assertEquals(user2.getPrincipal(), items.getJsonObject(6).getString("principal"));
	}
	
	@Test
	void UC_4_05_FindAllOk_InvalidOffset() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "XXX");
		
		this.mockMvc.perform(get("/users").params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
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
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(1, page.getInt("total"));
		
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
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(6, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(6, items.size());
		
		// ordinamento default ID desc
		
		assertEquals("org_editor", items.getJsonObject(0).getString("principal"));
		assertEquals("org_viewer", items.getJsonObject(1).getString("principal"));
		assertEquals("user_editor", items.getJsonObject(2).getString("principal"));
		assertEquals("user_viewer", items.getJsonObject(3).getString("principal"));
		assertEquals("ospite", items.getJsonObject(4).getString("principal"));
		assertEquals("amministratore", items.getJsonObject(5).getString("principal"));
	}
	
	@Test
	void UC_4_08_FindAllOk_InvalidEnabled() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_ENABLED, "XXX");
		
		this.mockMvc.perform(get("/users").params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
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
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_ASC);
		
		MvcResult result = this.mockMvc.perform(get("/users").params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(8, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(8, items.size());
		
		assertEquals("amministratore", items.getJsonObject(0).getString("principal"));
		assertEquals("org_editor", items.getJsonObject(1).getString("principal"));
		assertEquals("user_viewer", items.getJsonObject(2).getString("principal"));
		assertEquals("ospite", items.getJsonObject(3).getString("principal"));
		assertEquals(user.getPrincipal(), items.getJsonObject(4).getString("principal"));
		assertEquals("user_editor", items.getJsonObject(5).getString("principal"));
		assertEquals("org_viewer", items.getJsonObject(6).getString("principal"));
		assertEquals(user2.getPrincipal(), items.getJsonObject(7).getString("principal"));
		
	}
	
	@Test
	void UC_4_10_FindAllOk_SortId() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();
		UserEntity user2 = Costanti.getUser_Vbuterin();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "id");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_ASC);
		
		MvcResult result = this.mockMvc.perform(get("/users").params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(8, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(8, items.size());
		
		assertEquals("amministratore", items.getJsonObject(0).getString("principal"));
		assertEquals("ospite", items.getJsonObject(1).getString("principal"));
		assertEquals("user_viewer", items.getJsonObject(2).getString("principal"));
		assertEquals("user_editor", items.getJsonObject(3).getString("principal"));
		assertEquals("org_viewer", items.getJsonObject(4).getString("principal"));
		assertEquals("org_editor", items.getJsonObject(5).getString("principal"));
		assertEquals(user.getPrincipal(), items.getJsonObject(6).getString("principal"));
		assertEquals(user2.getPrincipal(), items.getJsonObject(7).getString("principal"));
	}
	
	@Test
	void UC_4_11_FindAllOk_OffsetLimit() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "3");
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "2");
		
		MvcResult result = this.mockMvc.perform(get("/users").params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(2, page.getInt("offset"));
		assertEquals(2, page.getInt("limit"));
		assertEquals(8, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());
		
		assertEquals("org_editor", items.getJsonObject(0).getString("principal"));
		assertEquals("org_viewer", items.getJsonObject(1).getString("principal"));
	}
	
	@Test
	void UC_4_12_FindAllOk_SortFullnameDesc() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();
		UserEntity user2 = Costanti.getUser_Vbuterin();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "full_name");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);
		
		MvcResult result = this.mockMvc.perform(get("/users").params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(8, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(8, items.size());

		assertEquals(user2.getPrincipal(), items.getJsonObject(0).getString("principal"));
		assertEquals("org_viewer", items.getJsonObject(1).getString("principal"));
		assertEquals("user_editor", items.getJsonObject(2).getString("principal"));
		assertEquals(user.getPrincipal(), items.getJsonObject(3).getString("principal"));
		assertEquals("ospite", items.getJsonObject(4).getString("principal"));
		assertEquals("user_viewer", items.getJsonObject(5).getString("principal"));
		assertEquals("org_editor", items.getJsonObject(6).getString("principal"));
		assertEquals("amministratore", items.getJsonObject(7).getString("principal"));
	}
	
	@Test
	void UC_4_13_FindAllOk_SortIdDesc() throws Exception {
		UserEntity user = Costanti.getUser_Snakamoto();
		UserEntity user2 = Costanti.getUser_Vbuterin();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "id");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);
		
		MvcResult result = this.mockMvc.perform(get("/users").params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(8, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(8, items.size());

		assertEquals(user2.getPrincipal(), items.getJsonObject(0).getString("principal"));
		assertEquals(user.getPrincipal(), items.getJsonObject(1).getString("principal"));
		assertEquals("org_editor", items.getJsonObject(2).getString("principal"));
		assertEquals("org_viewer", items.getJsonObject(3).getString("principal"));
		assertEquals("user_editor", items.getJsonObject(4).getString("principal"));
		assertEquals("user_viewer", items.getJsonObject(5).getString("principal"));
		assertEquals("ospite", items.getJsonObject(6).getString("principal"));
		assertEquals("amministratore", items.getJsonObject(7).getString("principal"));
	}
	
	@Test
	void UC_4_14_FindAllOk_InvalidSortParam() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "XXX");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);
		
		this.mockMvc.perform(get("/users").params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
}
