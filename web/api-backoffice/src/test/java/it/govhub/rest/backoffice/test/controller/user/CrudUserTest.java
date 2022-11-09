package it.govhub.rest.backoffice.test.controller.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.Charset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govhub.rest.backoffice.Application;
import it.govhub.rest.backoffice.beans.User;
import it.govhub.rest.backoffice.beans.UserCreate;
import it.govhub.rest.backoffice.test.utils.UserAuthProfilesUtils;



@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di censimento Utenti")
public class CrudUserTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Test
	@DisplayName("Creazione di un nuovo utente")
	public void creazioneUtente() throws Exception {
		UserCreate user = new UserCreate();
		user.setEmail("mimmo@napo.li");
		user.setEnabled(false);
		user.setFullName("Mimmo mariano");
		user.setPrincipal("mimmo-mariano");
	
		String content = mapper.writeValueAsString(user);
		
		MvcResult result = this.mockMvc.perform(post("/users")
				.with(UserAuthProfilesUtils.utenzaAdmin())
				.content(content)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andReturn();
		
		// Recupero il dettaglio dell'utente e confronto che i campi ci siano tutti
		String resultString = result.getResponse().getContentAsString(Charset.forName("UTF-8"));
		User created = mapper.readValue(resultString, User.class);
		
		assertEquals(user.getEmail(), created.getEmail());
		assertEquals(user.getEnabled(), created.getEnabled());
		assertEquals(user.getFullName(), created.getFullName());
		assertEquals(user.getPrincipal(), created.getPrincipal());
	}

}
