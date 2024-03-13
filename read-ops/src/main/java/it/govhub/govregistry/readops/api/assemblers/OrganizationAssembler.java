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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.api.beans.Organization;
import it.govhub.govregistry.commons.api.beans.OrganizationCreate;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.readops.api.spec.OrganizationApi;

@Component
public class OrganizationAssembler extends RepresentationModelAssemblerSupport<OrganizationEntity, Organization> {
	
	Logger log = LoggerFactory.getLogger(OrganizationAssembler.class);

	public OrganizationAssembler() {
		super(OrganizationApi.class, Organization.class);
	}
	

	@Override
	public Organization toModel(OrganizationEntity src) {
		log.debug("Assembling Entity [Organization] to model...");
		Organization ret = instantiateModel(src);
		
		BeanUtils.copyProperties(src, ret);
		
		ret.add(linkTo(
				methodOn(OrganizationApi.class)
				.readOrganization(src.getId()))
			.withSelfRel()
		);
		
		if(src.getLogo() != null) {
			ret.add(linkTo(
					methodOn(OrganizationApi.class)
					.downloadOrganizationLogo(src.getId()))
				.withRel("logo"));
		}
		
		if (src.getLogoMiniature() != null) {
			ret.add(linkTo(
						methodOn(OrganizationApi.class)
						.downloadOrganizationLogoMiniature(src.getId()))
				.withRel("logo-miniature"));
		}
			
		return ret;
	}

	
	public OrganizationEntity toEntity(OrganizationCreate src) {
		log.debug("Converting Model [OrganizationCreate] to Entity");

		OrganizationEntity ret = new OrganizationEntity();
		BeanUtils.copyProperties(src, ret);

		return ret;
	}
	

}
