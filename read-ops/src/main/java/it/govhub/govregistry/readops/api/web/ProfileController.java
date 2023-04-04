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
package it.govhub.govregistry.readops.api.web;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import it.govhub.govregistry.commons.api.beans.Authorization;
import it.govhub.govregistry.commons.api.beans.Profile;
import it.govhub.govregistry.commons.config.ApplicationConfig;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.readops.api.assemblers.ProfileAssembler;
import it.govhub.govregistry.readops.api.spec.ProfileApi;
import it.govhub.security.beans.GovhubPrincipal;


@V1RestController
public class ProfileController implements ProfileApi {
	
	@Autowired
	ProfileAssembler profileAssembler;
	
	@Autowired
	ApplicationConfig appConfig;
	
	Logger log = LoggerFactory.getLogger(ProfileController.class);
	
	@Override
	public ResponseEntity<Profile> profile() {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		GovhubPrincipal principal = (GovhubPrincipal) authentication.getPrincipal();
		
		Profile ret = this.profileAssembler.toModel(principal.getUser());
		
		log.debug("Assembling Entity [User] to model, filtering out authorizations by applicationId [{}]", this.appConfig.getApplicationId());
		
		OffsetDateTime now = OffsetDateTime.now();

		List<Authorization> newAuthorizations = ret.getAuthorizations().stream()
				.filter( auth -> auth.getExpirationDate() == null || now.compareTo(auth.getExpirationDate()) < 0 )
				.filter( auth -> auth.getRole().getApplication().equals(this.appConfig.getApplicationId()))
				.collect(Collectors.toList());
		
		ret.setAuthorizations(newAuthorizations);
		
		return ResponseEntity.ok(ret);
	}

}
