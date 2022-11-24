package it.govhub.rest.backoffice.test.controller.service;

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
import it.govhub.rest.backoffice.entity.ServiceEntity;
import it.govhub.rest.backoffice.repository.ServiceRepository;
import it.govhub.rest.backoffice.test.Costanti;
import it.govhub.rest.backoffice.test.utils.UserAuthProfilesUtils;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura dei servizi")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

public class Service_UC_4_FindServicesTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ServiceRepository serviceRepository;
	
	@BeforeEach
	private void caricaServizi() {
		ServiceEntity servizio = Costanti.getServizioTest();
		this.serviceRepository.save(servizio);
	}
	
	@Test
	void UC_4_01_FindAllOk() throws Exception {
		ServiceEntity servizio = Costanti.getServizioTest();
		
		MvcResult result = this.mockMvc.perform(get("/services")
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
		assertEquals(9, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(9, items.size());
		
		// ordinamento di default ID desc
		
		assertEquals(servizio.getName(), items.getJsonObject(0).getString("service_name"));
		assertEquals("Servizi Turistici", items.getJsonObject(1).getString("service_name"));
		assertEquals("Variazione Residenza", items.getJsonObject(2).getString("service_name"));
		assertEquals("Portale ZTL", items.getJsonObject(3).getString("service_name"));
		assertEquals("TARI", items.getJsonObject(4).getString("service_name"));
		assertEquals("IMU-ImpostaMunicipaleUnica", items.getJsonObject(5).getString("service_name"));
		assertEquals("SUAP-Integrazione", items.getJsonObject(6).getString("service_name"));
		assertEquals("Servizio senza autorizzazioni", items.getJsonObject(7).getString("service_name"));		
		assertEquals("Servizio Generico", items.getJsonObject(8).getString("service_name"));
	}
	
	@Test
	void UC_4_02_FindAllOk_Limit() throws Exception {
		ServiceEntity servizio = Costanti.getServizioTest();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "3");
		
		MvcResult result = this.mockMvc.perform(get("/services").params(params )
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
		assertEquals(9, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(3, items.size());
		
		// ordinamento di default ID desc
		
		assertEquals(servizio.getName(), items.getJsonObject(0).getString("service_name"));
		assertEquals("Servizi Turistici", items.getJsonObject(1).getString("service_name"));
		assertEquals("Variazione Residenza", items.getJsonObject(2).getString("service_name"));
		
	}
	
	@Test
	void UC_4_03_FindAllOk_InvalidLimit() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "XXX");
		
		this.mockMvc.perform(get("/services").params(params )
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
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "1");
		
		MvcResult result = this.mockMvc.perform(get("/services").params(params )
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
		assertEquals(9, page.getInt("total"));
		
		// Controlli sugli items
//		JsonArray items = userList.getJsonArray("items");
//		assertEquals(7, items.size());
	}
	
	@Test
	void UC_4_05_FindAllOk_InvalidOffset() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "XXX");
		
		this.mockMvc.perform(get("/services").params(params )
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
		ServiceEntity servizio = Costanti.getServizioTest();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_Q, "Generica");
		
		MvcResult result = this.mockMvc.perform(get("/services").params(params )
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
		assertEquals(1, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		JsonObject item1 = items.getJsonObject(0);
		assertEquals(servizio.getName(), item1.getString("service_name"));
		assertEquals(servizio.getDescription(), item1.getString("description"));
		
	}
	
	@Test
	void UC_4_09_FindAllOk_SortServiceName() throws Exception {
		ServiceEntity servizio = Costanti.getServizioTest();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "service_name");
		
		MvcResult result = this.mockMvc.perform(get("/services").params(params )
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
		assertEquals(9, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(9, items.size());
		
		assertEquals("IMU-ImpostaMunicipaleUnica", items.getJsonObject(0).getString("service_name"));
		assertEquals("Portale ZTL", items.getJsonObject(1).getString("service_name"));
		assertEquals("SUAP-Integrazione", items.getJsonObject(2).getString("service_name"));
		assertEquals("Servizi Turistici", items.getJsonObject(3).getString("service_name"));
		assertEquals("Servizio Generico", items.getJsonObject(4).getString("service_name"));
		assertEquals(servizio.getName(), items.getJsonObject(5).getString("service_name"));
		assertEquals("Servizio senza autorizzazioni", items.getJsonObject(6).getString("service_name"));
		assertEquals("TARI", items.getJsonObject(7).getString("service_name"));
		assertEquals("Variazione Residenza", items.getJsonObject(8).getString("service_name"));
	}
	
	@Test
	void UC_4_10_FindAllOk_SortId() throws Exception {
		ServiceEntity servizio = Costanti.getServizioTest();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "id");
		
		MvcResult result = this.mockMvc.perform(get("/services").params(params )
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
		assertEquals(9, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(9, items.size());
		
		assertEquals("Servizio Generico", items.getJsonObject(0).getString("service_name"));
		assertEquals("Servizio senza autorizzazioni", items.getJsonObject(1).getString("service_name"));
		assertEquals("SUAP-Integrazione", items.getJsonObject(2).getString("service_name"));
		assertEquals("IMU-ImpostaMunicipaleUnica", items.getJsonObject(3).getString("service_name"));
		assertEquals("TARI", items.getJsonObject(4).getString("service_name"));
		assertEquals("Portale ZTL", items.getJsonObject(5).getString("service_name"));
		assertEquals("Variazione Residenza", items.getJsonObject(6).getString("service_name"));
		assertEquals("Servizi Turistici", items.getJsonObject(7).getString("service_name"));
		assertEquals(servizio.getName(), items.getJsonObject(8).getString("service_name"));
	}
	
	@Test
	void UC_4_11_FindAllOk_OffsetLimit() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "3");
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "2");
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "id");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_ASC);
		
		MvcResult result = this.mockMvc.perform(get("/services").params(params )
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(2, page.getInt("offset"));
		assertEquals(2, page.getInt("limit"));
		assertEquals(9, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());
		
		assertEquals("Variazione Residenza", items.getJsonObject(0).getString("service_name"));
		assertEquals("Portale ZTL", items.getJsonObject(1).getString("service_name"));
	}
	
	@Test
	void UC_4_12_FindAllOk_SortServiceNameDesc() throws Exception {
		ServiceEntity servizio = Costanti.getServizioTest();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "service_name");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);
		
		MvcResult result = this.mockMvc.perform(get("/services").params(params )
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
		assertEquals(9, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(9, items.size());

		assertEquals("Variazione Residenza", items.getJsonObject(0).getString("service_name"));
		assertEquals("TARI", items.getJsonObject(1).getString("service_name"));
		assertEquals("Servizio senza autorizzazioni", items.getJsonObject(2).getString("service_name"));
		assertEquals(servizio.getName(), items.getJsonObject(3).getString("service_name"));
		assertEquals("Servizio Generico", items.getJsonObject(4).getString("service_name"));
		assertEquals("Servizi Turistici", items.getJsonObject(5).getString("service_name"));
		assertEquals("SUAP-Integrazione", items.getJsonObject(6).getString("service_name"));
		assertEquals("Portale ZTL", items.getJsonObject(7).getString("service_name"));
		assertEquals("IMU-ImpostaMunicipaleUnica", items.getJsonObject(8).getString("service_name"));
	}
	
	@Test
	void UC_4_13_FindAllOk_SortIdDesc() throws Exception {
		ServiceEntity servizio = Costanti.getServizioTest();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "id");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);
		
		MvcResult result = this.mockMvc.perform(get("/services").params(params )
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
		assertEquals(9, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(9, items.size());

		assertEquals(servizio.getName(), items.getJsonObject(0).getString("service_name"));
		assertEquals("Servizi Turistici", items.getJsonObject(1).getString("service_name"));
		assertEquals("Variazione Residenza", items.getJsonObject(2).getString("service_name"));
		assertEquals("Portale ZTL", items.getJsonObject(3).getString("service_name"));
		assertEquals("TARI", items.getJsonObject(4).getString("service_name"));
		assertEquals("IMU-ImpostaMunicipaleUnica", items.getJsonObject(5).getString("service_name"));
		assertEquals("SUAP-Integrazione", items.getJsonObject(6).getString("service_name"));
		assertEquals("Servizio senza autorizzazioni", items.getJsonObject(7).getString("service_name"));		
		assertEquals("Servizio Generico", items.getJsonObject(8).getString("service_name"));
		
	}
	
	@Test
	void UC_4_14_FindAllOk_InvalidSortParam() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "XXX");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);
		
		this.mockMvc.perform(get("/services").params(params )
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
}