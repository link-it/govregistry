package it.govhub.rest.backoffice.test.controller.organization;

import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import it.govhub.rest.backoffice.Application;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura delle Organizations")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

public class UC_1_FindOrganizationsTest {

}
