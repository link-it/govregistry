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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.api.beans.ServiceAuthItem;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.readops.api.spec.ServiceApi;

@Component
public class ServiceAuthItemAssembler extends RepresentationModelAssemblerSupport<ServiceEntity, ServiceAuthItem>{
	
	Logger log = LoggerFactory.getLogger(ServiceAuthItemAssembler.class);

	public ServiceAuthItemAssembler() {
		super(ServiceApi.class, ServiceAuthItem.class);
	}

	
	@Override
	public ServiceAuthItem toModel(ServiceEntity src) {
		log.debug("Assembling Entity [Service] to model...");
		ServiceAuthItem ret = instantiateModel(src);
		
		BeanUtils.copyProperties(src, ret);
		ret.setServiceName(src.getName());
		
		ret.add(linkTo(
				methodOn(ServiceApi.class)
				.readService(src.getId()))
			.withSelfRel()
		) ;
		
		if (src.getLogo() != null) {
			ret.add(linkTo(
					methodOn(ServiceApi.class)
					.downloadServiceLogo(src.getId()))
					.withRel("logo"));
		}
		
		if (src.getLogoMiniature() != null) { 
			ret.add(linkTo(
					methodOn(ServiceApi.class)
					.downloadServiceLogoMiniature(src.getId()))
				.withRel("logo-miniature")
			) ;
		}
		
		return ret; 
	}	

}
