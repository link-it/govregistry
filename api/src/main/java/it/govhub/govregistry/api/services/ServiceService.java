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
package it.govhub.govregistry.api.services;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import it.govhub.govregistry.api.repository.ServiceRepository;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.exception.ConflictException;
import it.govhub.govregistry.commons.exception.SemanticValidationException;
import it.govhub.govregistry.commons.messages.ServiceMessages;
import it.govhub.govregistry.readops.api.assemblers.ServiceAssembler;

@org.springframework.stereotype.Service
public class ServiceService {
	
	@Autowired
	ServiceRepository serviceRepo;
	
	@Autowired
	ServiceAssembler serviceAssembler;
	
	
	@Autowired
	ServiceMessages serviceMessages;
	
	Logger log = LoggerFactory.getLogger(ServiceService.class);
	

	@Transactional
	public ServiceEntity createService(ServiceEntity service) {
		log.info("Creating new service: {}", service);
		if (this.serviceRepo.findByName(service.getName()).isPresent()) {
			throw new ConflictException(this.serviceMessages.conflict("name", service.getName()));
		}
		return this.serviceRepo.save(service);
	}

	@Transactional
	public ServiceEntity replaceService(ServiceEntity oldService, ServiceEntity newService) {
		log.info("Replacing Service {} With {}", oldService, newService);
		
		if ( ! oldService.getId().equals(newService.getId())) {
			throw new SemanticValidationException(this.serviceMessages.fieldNotModificable("id"));
		}
		
		var conflictService = this.serviceRepo.findByName(newService.getName()).
				filter( s -> !s.getId().equals(newService.getId()));
		
		if (conflictService.isPresent() ) {
			throw new ConflictException(this.serviceMessages.conflict("name", newService.getName()));
		}
		
		return this.serviceRepo.save(newService);
	}


}
