/*
 * GovRegistry - Registries manager for GovHub
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
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
import it.govhub.govregistry.api.repository.ServiceRepository;
import it.govhub.govregistry.api.test.Costanti;
import it.govhub.govregistry.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.ServiceEntity;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di censimento proprieta logo e logo_miniature di un service")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

class Service_UC_7_ServiceLogoTest {

	private static final String SERVICES_BASE_PATH = "/v1/services";
	private static final String SERVICES_BASE_PATH_DETAIL_ID = SERVICES_BASE_PATH + "/{id}";
	private static final String SERVICES_BASE_PATH_LOGO = SERVICES_BASE_PATH_DETAIL_ID + "/logo";
	private static final String SERVICES_BASE_PATH_LOGO_MINIATURE = SERVICES_BASE_PATH_DETAIL_ID + "/logo-miniature";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ServiceRepository serviceRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@BeforeEach
	private void configurazioneDB() {
		ServiceEntity service = Costanti.getServizioTest();
		service.setLogo(null);
		service.setLogoMiniature(null);
		this.serviceRepository.save(service);
	}
	
	private ServiceEntity leggiServizioDB(String nome) {
		List<ServiceEntity> findAll = this.serviceRepository.findAll();
		return findAll.stream().filter(f -> f.getName().equals(nome)).collect(Collectors.toList()).get(0);
	}
	
	@Test
	void UC_7_01_PathchService_ReplaceLogoMiniature() throws Exception {
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		Long id = servizio.getId();

		this.mockMvc.perform(put(SERVICES_BASE_PATH_LOGO_MINIATURE, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(Base64.decodeBase64(Costanti.LOGO_ENTE_CREDITORE_3))
				.contentType("image/png"))
				.andExpect(status().isOk())
				.andReturn();

		// rileggo l'ente dal DB e controllo che sia presente il campo logo
		servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		
		assertNotNull(servizio.getLogoMiniature());
		
	}
	
	@Test
	void UC_7_02_PathchService_ReplaceLogo() throws Exception {
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		Long id = servizio.getId();

		this.mockMvc.perform(put(SERVICES_BASE_PATH_LOGO, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(Base64.decodeBase64(Costanti.LOGO_ENTE_CREDITORE_3))
				.contentType("image/png"))
				.andExpect(status().isOk())
				.andReturn();

		// rileggo l'ente dal DB e controllo che sia presente il campo logo
		servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		
		assertNotNull(servizio.getLogo());
		
	}
	
	@Test
	void UC_7_03_PathchService_DeleteLogoMiniature() throws Exception {
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		Long id = servizio.getId();

		this.mockMvc.perform(put(SERVICES_BASE_PATH_LOGO_MINIATURE, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(Base64.decodeBase64(Costanti.LOGO_ENTE_CREDITORE_3))
				.contentType("image/png"))
				.andExpect(status().isOk())
				.andReturn();

		// rileggo l'ente dal DB e controllo che sia presente il campo logo
		servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		
		assertNotNull(servizio.getLogoMiniature());
		
		this.mockMvc.perform(delete(SERVICES_BASE_PATH_LOGO_MINIATURE, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				)
				.andExpect(status().isOk())
				.andReturn();

		// rileggo l'ente dal DB e controllo che sia presente il campo logo
		servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		
		assertNull(servizio.getLogoMiniature());
		
		
	}
	
	@Test
	void UC_7_04_PathchService_DeleteLogo() throws Exception {
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		Long id = servizio.getId();

		this.mockMvc.perform(put(SERVICES_BASE_PATH_LOGO, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(Base64.decodeBase64(Costanti.LOGO_ENTE_CREDITORE_3))
				.contentType("image/png"))
				.andExpect(status().isOk())
				.andReturn();

		// rileggo l'ente dal DB e controllo che sia presente il campo logo
		servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		
		assertNotNull(servizio.getLogo());
		
		this.mockMvc.perform(delete(SERVICES_BASE_PATH_LOGO, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				)
				.andExpect(status().isOk())
				.andReturn();

		// rileggo l'ente dal DB e controllo che sia presente il campo logo
		servizio = leggiServizioDB(Costanti.SERVICE_NAME_TEST);
		
		assertNull(servizio.getLogo());
		
	}
	
	@Test
	void UC_7_05_PathchService_LogoMiniature_ServiceNotFound() throws Exception {
		int idUser1 = 10000;
		
		this.mockMvc.perform(put(SERVICES_BASE_PATH_LOGO_MINIATURE,idUser1)
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
		
		this.mockMvc.perform(delete(SERVICES_BASE_PATH_LOGO_MINIATURE,idUser1)
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
	void UC_7_06_PathchService_LogoMiniature_ServiceInvalidId() throws Exception {
		String idUser1 = "XXX";
		
		this.mockMvc.perform(put(SERVICES_BASE_PATH_LOGO_MINIATURE,idUser1)
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
		
		this.mockMvc.perform(delete(SERVICES_BASE_PATH_LOGO_MINIATURE,idUser1)
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
	void UC_7_07_PathchService_Logo_ServiceNotFound() throws Exception {
		int idUser1 = 10000;
		
		this.mockMvc.perform(put(SERVICES_BASE_PATH_LOGO,idUser1)
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
		
		this.mockMvc.perform(delete(SERVICES_BASE_PATH_LOGO,idUser1)
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
	void UC_7_08_PathchService_Logo_ServiceInvalidId() throws Exception {
		String idUser1 = "XXX";
		
		this.mockMvc.perform(put(SERVICES_BASE_PATH_LOGO,idUser1)
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
		
		this.mockMvc.perform(delete(SERVICES_BASE_PATH_LOGO,idUser1)
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
