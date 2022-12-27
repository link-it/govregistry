package it.govhub.govregistry.api.test.controller.service;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import javax.json.Json;
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
import it.govhub.govregistry.api.test.Costanti;
import it.govhub.govregistry.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.api.beans.PatchOp.OpEnum;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.readops.api.repository.ServiceRepository;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di update dei Services")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

class Service_UC_3_PatchServiceTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ServiceRepository serviceRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Test
	void UC_3_01_PatchService_WrongMediaType() throws Exception {
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/service_name")
				.add("value", "12345678901");

		String PatchService = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/services/{id}", 1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchService)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().is(400))
		.andExpect(jsonPath("$.detail", is("Content type 'application/json' not supported")))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type", is("https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request")))
		.andReturn();
	}

	@Test
	void UC_3_02_PatchService_WrongPayload() throws Exception {
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

		// Creo un messaggio di patch errato, la specifica prevede che venga inviato un'array di operazioni
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		String PatchService = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/service_name")
				.add("value", "12345678901")
				.build()
				.toString();

		this.mockMvc.perform(patch("/services/{id}", id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchService)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().is(400))
		.andExpect(jsonPath("$.detail").isString())
		.andExpect(jsonPath("$.type", is("https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request")))
		.andReturn();

		ServiceEntity serviceEntity = this.serviceRepository.findById((long) id).get();
		
		assertEquals(id, serviceEntity.getId());
		assertEquals(servizio.getName(), serviceEntity.getName());
		assertEquals(servizio.getDescription(), serviceEntity.getDescription());
	}

	@Test
	void UC_3_03_PatchService_Name() throws Exception {
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
				.with(this.userAuthProfilesUtils.utenzaAdmin())
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

	@Test
	void UC_3_04_PatchService_RemoveName() throws Exception {
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
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchService)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();

		ServiceEntity serviceEntity = this.serviceRepository.findById((long) id).get();
		
		assertEquals(id, serviceEntity.getId());
		assertEquals(servizio.getName(), serviceEntity.getName());
		assertEquals(servizio.getDescription(), serviceEntity.getDescription());
	}
	
	@Test
	void UC_3_05_PatchService_ReplaceDescription() throws Exception {
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

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		// Modifico il servizio
		servizio.setDescription("Descrizione aggiornata");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/description")
				.add("value", servizio.getDescription());

		String PatchService = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/services/{id}", id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
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

	@Test
	void UC_3_06_PatchService_AddDescription() throws Exception {
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

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		// Modifico il servizio
		servizio.setDescription("Descrizione aggiornata");

		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.ADD.toString())
				.add("path", "/description")
				.add("value", servizio.getDescription());

		String PatchService = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/services/{id}", id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
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

	@Test
	void UC_3_07_PatchService_RemoveDescription() throws Exception {
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

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		// Modifico il servizio
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REMOVE.toString())
				.add("path", "/description")
				.add("value", "");

		String PatchService = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/services/{id}", id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchService)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").isNumber())
		.andExpect(jsonPath("$.service_name", is(servizio.getName())))
		.andReturn();

		ServiceEntity serviceEntity = this.serviceRepository.findById((long) id).get();
		
		assertEquals(id, serviceEntity.getId());
		assertEquals(servizio.getName(), serviceEntity.getName());
		assertNull(serviceEntity.getDescription());
		
	}

	@Test
	void UC_3_08_PatchService_ReplaceNotExistingDescription() throws Exception {
		ServiceEntity servizio = Costanti.getServizioTest();

		String json = Json.createObjectBuilder()
				.add("service_name", servizio.getName())
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
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		// Modifico il servizio
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/description")
				.add("value", "Descrizione aggiornata");

		String PatchService = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch("/services/{id}", id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(PatchService)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();

		ServiceEntity serviceEntity = this.serviceRepository.findById((long) id).get();
		
		assertEquals(id, serviceEntity.getId());
		assertEquals(servizio.getName(), serviceEntity.getName());
		assertNull(serviceEntity.getDescription());
	}
}
