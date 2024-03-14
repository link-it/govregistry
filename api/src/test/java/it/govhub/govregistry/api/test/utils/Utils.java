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
package it.govhub.govregistry.api.test.utils;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import it.govhub.govregistry.api.repository.OrganizationRepository;
import it.govhub.govregistry.api.repository.ServiceRepository;
import it.govhub.govregistry.api.repository.UserRepository;
import it.govhub.govregistry.api.test.Costanti;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.RoleEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.repository.ReadRoleRepository;

/***
 * Utilities comuni ai test
 */
public class Utils {

	public static RoleEntity leggiRuoloDB(String nomeRuolo, ReadRoleRepository roleRepository) {
		List<RoleEntity> findAll = roleRepository.findAll();
		List<RoleEntity> collect = findAll.stream().filter(f -> f.getName().equals(nomeRuolo)).collect(Collectors.toList());
		return collect.size()> 0 ? collect.get(0) : null;
	}
	
	public static UserEntity leggiUtenteDB(String principal, UserRepository userRepository) {
		List<UserEntity> findAll = userRepository.findAll();
		List<UserEntity> collect = findAll.stream().filter(f -> f.getPrincipal().equals(principal)).collect(Collectors.toList());
		return collect.size()> 0 ? collect.get(0) : null;
	}
	
	public static OrganizationEntity leggiEnteDB(String nome, OrganizationRepository organizationRepository) {
		List<OrganizationEntity> findAll = organizationRepository.findAll();
		List<OrganizationEntity> collect = findAll.stream().filter(f -> f.getTaxCode().equals(nome)).collect(Collectors.toList());
		return collect.size()> 0 ? collect.get(0) : null;
	}
	
	public static ServiceEntity leggiServizioDB(String nome, ServiceRepository serviceRepository) {
		List<ServiceEntity> findAll = serviceRepository.findAll();
		List<ServiceEntity> collect = findAll.stream().filter(f -> f.getName().equals(nome)).collect(Collectors.toList());
		return collect.size()> 0 ? collect.get(0) : null;
	}
	
	public static void deleteAllAuthorizations(UserEntity user, MockMvc mockMvc, RequestPostProcessor utenza) throws Exception {
		MvcResult result = mockMvc.perform(get(Costanti.USERS_ID_AUTHORIZATIONS_BASE_PATH, user.getId())
				.with(utenza)
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Leggo la lista delle autorizzazioni
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		
		if(items != null && items.size() > 0) {
			for (int i = 0; i < items.size(); i++) {
				JsonObject item = items.getJsonObject(i);
				int idRole = item.getInt("id");
			
				// Cancellazione Autorizzazione
				cancellaAutorizzazione(user.getId().intValue(), idRole, mockMvc, utenza);
			}
		}
	}
	
	public static void cancellaAutorizzazione(int uid, int aid, MockMvc mockMvc, RequestPostProcessor utenza) throws Exception {
		mockMvc.perform(delete(Costanti.AUTHORIZATIONS_BASE_PATH_DETAIL_ID, uid, aid)
				.with(utenza)
				.with(csrf())
				.accept("*/*"))
				.andExpect(status().isOk())
				.andReturn();
	}
}
