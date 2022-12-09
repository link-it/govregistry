package it.govhub.govregistry.application.test.controller.system;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;

import it.govhub.govregistry.application.Application;
import it.govhub.govregistry.application.test.utils.UserAuthProfilesUtils;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura degli Utenti")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class System_UC_2_GetStatusTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Test
	void UC_2_01_GetStatusOk() throws Exception {
		this.mockMvc.perform(get("/status")
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept("application/problem+json"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is(200)))
				.andExpect(jsonPath("$.title", is("OK")))
				.andExpect(jsonPath("$.type", is("https://www.rfc-editor.org/rfc/rfc9110.html#name-200-ok")))
				.andExpect(jsonPath("$.detail", is("System is working correctly")))
				.andReturn();
	}
}
