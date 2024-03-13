/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govhub.govregistry.api.test.controller.service;

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
import it.govhub.govregistry.api.repository.ServiceRepository;
import it.govhub.govregistry.api.test.Costanti;
import it.govhub.govregistry.api.test.utils.Utils;
import it.govhub.govregistry.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.ServiceEntity;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura dei servizi")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Service_UC_4_FindServicesTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ServiceRepository serviceRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	private void configurazioneDB() {
		ServiceEntity service = Costanti.getServizioTest();
		if(leggiServizioDB(service.getName()) == null) {
			this.serviceRepository.save(service);
		}
	}
	
	private ServiceEntity leggiServizioDB(String nome) {
		return Utils.leggiServizioDB(nome, this.serviceRepository);
	}
	
	@Test
	void UC_4_01_FindAllOk() throws Exception {
		configurazioneDB();
		ServiceEntity servizio = Costanti.getServizioTest();
		
		MvcResult result = this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH)
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
		configurazioneDB();
		ServiceEntity servizio = Costanti.getServizioTest();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "3");
		
		MvcResult result = this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH).params(params )
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
		
		this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH).params(params )
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
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "1");
		
		MvcResult result = this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH).params(params )
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
		assertEquals(9, page.getInt("total"));
		
		// Controlli sugli items
//		JsonArray items = userList.getJsonArray("items");
//		assertEquals(7, items.size());
	}
	
	@Test
	void UC_4_05_FindAllOk_InvalidOffset() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "XXX");
		
		this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH).params(params )
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
		configurazioneDB();
		ServiceEntity servizio = Costanti.getServizioTest();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_Q, "Generica");
		
		MvcResult result = this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH).params(params )
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
		assertEquals(servizio.getName(), item1.getString("service_name"));
		assertEquals(servizio.getDescription(), item1.getString("description"));
		
	}
	
	@Test
	void UC_4_09_FindAllOk_SortServiceName() throws Exception {
		configurazioneDB();
		ServiceEntity servizio = Costanti.getServizioTest();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "service_name");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_ASC);
		
		MvcResult result = this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH).params(params )
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
		configurazioneDB();
		ServiceEntity servizio = Costanti.getServizioTest();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "id");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_ASC);
		
		MvcResult result = this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH).params(params )
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
		configurazioneDB();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "3");
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "2");
		
		MvcResult result = this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH).params(params )
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
		assertEquals(9, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());
		
		assertEquals("Variazione Residenza", items.getJsonObject(0).getString("service_name"));
		assertEquals("Portale ZTL", items.getJsonObject(1).getString("service_name"));
	}
	
	@Test
	void UC_4_12_FindAllOk_SortServiceNameDesc() throws Exception {
		configurazioneDB();
		ServiceEntity servizio = Costanti.getServizioTest();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "service_name");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);
		
		MvcResult result = this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH).params(params )
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
		configurazioneDB();
		ServiceEntity servizio = Costanti.getServizioTest();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "id");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);
		
		MvcResult result = this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH).params(params )
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
		
		this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH).params(params )
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
	void UC_4_15_FindAllOk_Sort_Unsorted() throws Exception {
		configurazioneDB();
		ServiceEntity servizio = Costanti.getServizioTest();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "unsorted");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_ASC);
		
		MvcResult result = this.mockMvc.perform(get(Costanti.SERVICES_BASE_PATH).params(params )
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
}
