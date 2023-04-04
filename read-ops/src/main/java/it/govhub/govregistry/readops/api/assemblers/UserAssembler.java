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
package it.govhub.govregistry.readops.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.api.beans.User;
import it.govhub.govregistry.commons.api.beans.UserCreate;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.spec.UserApi;

@Component
public class UserAssembler  extends RepresentationModelAssemblerSupport<UserEntity, User> {
	
	Logger log = LoggerFactory.getLogger(UserAssembler.class);

	public UserAssembler() {
		super(UserApi.class, User.class);
	}

	@Override
	public User toModel(UserEntity src) {
		log.debug("Assembling Entity [User] to model...");
		User ret = instantiateModel(src);
		
        BeanUtils.copyProperties(src, ret);

		ret.add(linkTo(
					methodOn(UserApi.class)
					.readUser(src.getId()))
				.withSelfRel()
			);
		
		if (src.getEmail() != null) {
			String md5 = DigestUtils.md5Hex(src.getEmail());
			ret.add(
					Link.of("https://gravatar.com/avatar/"+md5+"/s=100&d=identicon", LinkRelation.of("avatar")));
		}
		
		return ret;
	}
	

	public UserEntity toEntity(User src) {
		log.debug("Converting Model [User] to entity...");
		UserEntity entity = new UserEntity();
		BeanUtils.copyProperties(src, entity);
		return entity;
	}
	
	
	public UserEntity toEntity(UserCreate src) {
		log.debug("Converting Model [UserCreate] to entity...");
		UserEntity entity = new UserEntity();
		BeanUtils.copyProperties(src, entity);
		return entity;
	}

}
