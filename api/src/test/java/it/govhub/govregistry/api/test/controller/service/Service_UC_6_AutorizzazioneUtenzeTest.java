package it.govhub.govregistry.api.test.controller.service;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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

import it.govhub.govregistry.api.Application;
import it.govhub.govregistry.api.beans.PatchOp.OpEnum;
import it.govhub.govregistry.api.test.Costanti;
import it.govhub.govregistry.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.repository.ServiceRepository;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di controllo autorizzazioni necessarie all'esecuzione delle operazioni sui Servizi")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

class Service_UC_6_AutorizzazioneUtenzeTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ServiceRepository serviceRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	//1. CreateService con utenza non admin con ruolo govhub_services_editor: OK
	@Test
	void UC_6_01_CreateServiceOk_UtenzaConRuolo_GovHub_Services_Editor() throws Exception {
		ServiceEntity servizio = Costanti.getServizioTest();

		String json = Json.createObjectBuilder()
				.add("service_name", servizio.getName())
				.add("description", servizio.getDescription())
				.build()
				.toString();

		// Creo un service e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/services")
				.with(this.userAuthProfilesUtils.utenzaServiceEditor())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.service_name", is(servizio.getName())))
				.andExpect(jsonPath("$.description", is(servizio.getDescription())))
				.andReturn();
		
		// Leggo l'organization dal servizio e verifico
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		ServiceEntity serviceEntity = this.serviceRepository.findById((long) id).get();
		
		assertEquals(id, serviceEntity.getId());
		assertEquals(servizio.getName(), serviceEntity.getName());
		assertEquals(servizio.getDescription(), serviceEntity.getDescription());
		
	}
	
	//2. CreateService con utenza non admin con ruolo non govhub_services_editor: NotAuthorized
	@Test
	void UC_6_02_CreateServiceFail_UtenzaSenzaRuolo_GovHub_Services_Editor() throws Exception {
		ServiceEntity servizio = Costanti.getServizioTest();

		String json = Json.createObjectBuilder()
				.add("service_name", servizio.getName())
				.add("description", servizio.getDescription())
				.build()
				.toString();

		// Creo un service e verifico la risposta
		this.mockMvc.perform(post("/services")
				.with(this.userAuthProfilesUtils.utenzaServiceViewer())
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
	
	//3. PatchService con utenza non admin con ruolo govhub_services_editor: OK
	@Test
	void UC_6_03_PatchServiceOk_UtenzaConRuolo_GovHub_Services_Editor() throws Exception {
		ServiceEntity servizio = Costanti.getServizioTest();

		String json = Json.createObjectBuilder()
				.add("service_name", servizio.getName())
				.add("description", servizio.getDescription())
				.build()
				.toString();

		// Creo un service e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/services")
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.service_name", is(servizio.getName())))
				.andExpect(jsonPath("$.description", is(servizio.getDescription())))
				.andReturn();

		// Modifico il servizio
		servizio.setName("SSSSServizio");

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/service_name")
				.add("value", servizio.getName());

		String PatchService = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/services/{id}", id)
				.with(this.userAuthProfilesUtils.utenzaServiceEditor())
				.with(csrf())
				.content(PatchService)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.service_name", is(servizio.getName())))
		.andExpect(jsonPath("$.description", is(servizio.getDescription())))
		.andReturn();

		ServiceEntity serviceEntity = this.serviceRepository.findById((long) id).get();
		
		assertEquals(id, serviceEntity.getId());
		assertEquals(servizio.getName(), serviceEntity.getName());
		assertEquals(servizio.getDescription(), serviceEntity.getDescription());
	}
	
	//4. PatchService con utenza non admin con ruolo non govhub_services_editor: NotAuthorized
	@Test
	void UC_6_04_PatchServiceFail_UtenzaSenzaRuolo_GovHub_Services_Editor() throws Exception {
		ServiceEntity servizio = Costanti.getServizioTest();

		String json = Json.createObjectBuilder()
				.add("service_name", servizio.getName())
				.add("description", servizio.getDescription())
				.build()
				.toString();

		// Creo un service e verifico la risposta
		MvcResult result = this.mockMvc.perform(post("/services")
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.service_name", is(servizio.getName())))
				.andExpect(jsonPath("$.description", is(servizio.getDescription())))
				.andReturn();

		// Modifico il servizio cancellando il name
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REMOVE.toString())
				.add("path", "/service_name")
				.add("value", "");

		String PatchService = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/services/{id}", id)
				.with(this.userAuthProfilesUtils.utenzaServiceViewer())
				.with(csrf())
				.content(PatchService)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();

	}
	
	//5. FindAllServices con utenza non admin con ruolo govhub_services_editor/govhub_services_viewer: OK
	@Test
	void UC_6_05_FindAllOk_UtenzaConRuolo_GovHub_Services_Editor_O_Viewer() throws Exception {
		this.mockMvc.perform(get("/services")
				.with(this.userAuthProfilesUtils.utenzaServiceEditor())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		this.mockMvc.perform(get("/services")
				.with(this.userAuthProfilesUtils.utenzaServiceViewer())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
	}
	
	//6. FindAllServices con utenza non admin con ruolo non govhub_services_editor/govhub_services_viewer: NotAuthorized
	@Test
	void UC_6_06_FindAllFail_UtenzaSenzaRuolo_GovHub_Services_Editor_O_Viewer() throws Exception {
		this.mockMvc.perform(get("/services")
				.with(this.userAuthProfilesUtils.utenzaOspite())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	//7. GetService con utenza non admin con ruolo govhub_services_editor/govhub_services_viewer: OK
	@Test
	void UC_6_07_GetUserOk_UtenzaConRuolo_GovHub_Services_Editor_O_Viewer() throws Exception {
		MvcResult result = this.mockMvc.perform(get("/services/")
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		
		JsonObject item1 = items.getJsonObject(0); 
		int idService1 = item1.getInt("id");
		
		this.mockMvc.perform(get("/services/{id}",idService1)
				.with(this.userAuthProfilesUtils.utenzaServiceEditor())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		this.mockMvc.perform(get("/services/{id}",idService1)
				.with(this.userAuthProfilesUtils.utenzaServiceViewer())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
	}
	
	//8. GetService con utenza non admin con ruolo non govhub_services_editor/govhub_services_viewer: NotAuthorized
	@Test
	void UC_6_08_GetUserFail_UtenzaSenzaRuolo_GovHub_Services_Editor_O_Viewer() throws Exception {
		MvcResult result = this.mockMvc.perform(get("/services/")
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		
		JsonObject item1 = items.getJsonObject(0); 
		int idService1 = item1.getInt("id");
		
		this.mockMvc.perform(get("/services/{id}",idService1)
				.with(this.userAuthProfilesUtils.utenzaOspite())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
}

