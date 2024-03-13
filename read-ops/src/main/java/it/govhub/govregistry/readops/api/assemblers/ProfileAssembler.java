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
package it.govhub.govregistry.readops.api.assemblers;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.api.beans.Authorization;
import it.govhub.govregistry.commons.api.beans.Profile;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.web.ProfileController;

@Component
public class ProfileAssembler extends RepresentationModelAssemblerSupport<UserEntity, Profile> {
	
	@Autowired
	AuthorizationConverter authAssembler;
	
	Logger log = LoggerFactory.getLogger(ProfileAssembler.class);

	public ProfileAssembler() {
		super(ProfileController.class, Profile.class);
	}

	@Override
	public Profile toModel(UserEntity src) {
		log.debug("Assembling Entity [User] to model...");
		Profile ret = new Profile();
		
		BeanUtils.copyProperties(src, ret);
		
		List<Authorization> auths = src.getAuthorizations().stream()
			.map(this.authAssembler::toModel)
			.collect(Collectors.toList());
		
		ret.setAuthorizations(auths);
		
		return ret;
	}
	
}
