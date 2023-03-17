package it.govhub.govregistry.api.test.controller.organization;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;

import it.govhub.govregistry.api.Application;
import it.govhub.govregistry.api.repository.OrganizationRepository;
import it.govhub.govregistry.api.test.Costanti;
import it.govhub.govregistry.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.OrganizationEntity;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di censimento proprieta logo e logo_miniature di una Organization")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

class Organization_UC_7_OrganizationLogoTest {

	private static final String ORGANIZATIONS_BASE_PATH = "/v1/organizations";
	private static final String ORGANIZATIONS_BASE_PATH_DETAIL_ID = ORGANIZATIONS_BASE_PATH + "/{id}";
	private static final String ORGANIZATIONS_BASE_PATH_LOGO = ORGANIZATIONS_BASE_PATH_DETAIL_ID + "/logo";
	private static final String ORGANIZATIONS_BASE_PATH_LOGO_MINIATURE = ORGANIZATIONS_BASE_PATH_DETAIL_ID + "/logo-miniature";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@BeforeEach
	private void configurazioneDB() {
		OrganizationEntity ente = Costanti.getEnteCreditore3();
		ente.setLogo(null);
		ente.setLogoMiniature(null);
		this.organizationRepository.save(ente);
	}
	
	private OrganizationEntity leggiEnteDB(String nome) {
		List<OrganizationEntity> findAll = this.organizationRepository.findAll();
		return findAll.stream().filter(f -> f.getTaxCode().equals(nome)).collect(Collectors.toList()).get(0);
	}
	
	@Test
	void UC_7_01_PatchOrganization_ReplaceLogoMiniature() throws Exception {
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		Long id = ente.getId();

		this.mockMvc.perform(put(ORGANIZATIONS_BASE_PATH_LOGO_MINIATURE, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(Base64.decodeBase64(Costanti.LOGO_ENTE_CREDITORE_3))
				.contentType("image/png"))
				.andExpect(status().isOk())
				.andReturn();

		// rileggo l'ente dal DB e controllo che sia presente il campo logo
		ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		
		assertNotNull(ente.getLogoMiniature());
		
	}
	
	@Test
	void UC_7_02_PatchOrganization_ReplaceLogo() throws Exception {
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		Long id = ente.getId();

		this.mockMvc.perform(put(ORGANIZATIONS_BASE_PATH_LOGO, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(Base64.decodeBase64(Costanti.LOGO_ENTE_CREDITORE_3))
				.contentType("image/png"))
				.andExpect(status().isOk())
				.andReturn();

		// rileggo l'ente dal DB e controllo che sia presente il campo logo
		ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		
		assertNotNull(ente.getLogo());
		
	}
	
	@Test
	void UC_7_03_PatchOrganization_DeleteLogoMiniature() throws Exception {
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		Long id = ente.getId();

		this.mockMvc.perform(put(ORGANIZATIONS_BASE_PATH_LOGO_MINIATURE, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(Base64.decodeBase64(Costanti.LOGO_ENTE_CREDITORE_3))
				.contentType("image/png"))
				.andExpect(status().isOk())
				.andReturn();

		// rileggo l'ente dal DB e controllo che sia presente il campo logo
		ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		
		assertNotNull(ente.getLogoMiniature());
		
		this.mockMvc.perform(delete(ORGANIZATIONS_BASE_PATH_LOGO_MINIATURE, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				)
				.andExpect(status().isOk())
				.andReturn();

		// rileggo l'ente dal DB e controllo che sia presente il campo logo
		ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		
		assertNull(ente.getLogoMiniature());
		
		
	}
	
	@Test
	void UC_7_04_PatchOrganization_DeleteLogo() throws Exception {
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		Long id = ente.getId();

		this.mockMvc.perform(put(ORGANIZATIONS_BASE_PATH_LOGO, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(Base64.decodeBase64(Costanti.LOGO_ENTE_CREDITORE_3))
				.contentType("image/png"))
				.andExpect(status().isOk())
				.andReturn();

		// rileggo l'ente dal DB e controllo che sia presente il campo logo
		ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		
		assertNotNull(ente.getLogo());
		
		this.mockMvc.perform(delete(ORGANIZATIONS_BASE_PATH_LOGO, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				)
				.andExpect(status().isOk())
				.andReturn();

		// rileggo l'ente dal DB e controllo che sia presente il campo logo
		ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		
		assertNull(ente.getLogo());
		
	}
	
	@Test
	void UC_7_05_PatchOrganization_LogoMiniature_OrganizationNotFound() throws Exception {
		int idUser1 = 10000;
		
		this.mockMvc.perform(put(ORGANIZATIONS_BASE_PATH_LOGO_MINIATURE,idUser1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(Base64.decodeBase64(Costanti.LOGO_ENTE_CREDITORE_3))
				.contentType("image/png"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.title", is("Not Found")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
		this.mockMvc.perform(delete(ORGANIZATIONS_BASE_PATH_LOGO_MINIATURE,idUser1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.title", is("Not Found")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	@Test	
	void UC_7_06_PatchOrganization_LogoMiniature_OrganizationInvalidId() throws Exception {
		String idUser1 = "XXX";
		
		this.mockMvc.perform(put(ORGANIZATIONS_BASE_PATH_LOGO_MINIATURE,idUser1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(Base64.decodeBase64(Costanti.LOGO_ENTE_CREDITORE_3))
				.contentType("image/png"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
		this.mockMvc.perform(delete(ORGANIZATIONS_BASE_PATH_LOGO_MINIATURE,idUser1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	@Test
	void UC_7_07_PatchOrganization_Logo_OrganizationNotFound() throws Exception {
		int idUser1 = 10000;
		
		this.mockMvc.perform(put(ORGANIZATIONS_BASE_PATH_LOGO,idUser1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(Base64.decodeBase64(Costanti.LOGO_ENTE_CREDITORE_3))
				.contentType("image/png"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.title", is("Not Found")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
		this.mockMvc.perform(delete(ORGANIZATIONS_BASE_PATH_LOGO,idUser1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.title", is("Not Found")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	@Test	
	void UC_7_08_PatchOrganization_Logo_OrganizationInvalidId() throws Exception {
		String idUser1 = "XXX";
		
		this.mockMvc.perform(put(ORGANIZATIONS_BASE_PATH_LOGO,idUser1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(Base64.decodeBase64(Costanti.LOGO_ENTE_CREDITORE_3))
				.contentType("image/png"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
		this.mockMvc.perform(delete(ORGANIZATIONS_BASE_PATH_LOGO,idUser1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
}
