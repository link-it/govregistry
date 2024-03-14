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
package it.govhub.govregistry.readops.api.web;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import it.govhub.govregistry.commons.api.beans.Service;
import it.govhub.govregistry.commons.api.beans.ServiceList;
import it.govhub.govregistry.commons.api.beans.ServiceOrdering;
import it.govhub.govregistry.commons.config.ApplicationConfig;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.exception.NotAuthorizedException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.messages.ServiceMessages;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.govregistry.readops.api.assemblers.ServiceAssembler;
import it.govhub.govregistry.readops.api.assemblers.ServiceAuthItemAssembler;
import it.govhub.govregistry.readops.api.repository.ReadServiceRepository;
import it.govhub.govregistry.readops.api.repository.ServiceFilters;
import it.govhub.security.services.SecurityService;


/**
 * 
 * Tutte le applicazioni di Govhub hanno una stessa parte in comune per la lettura di utenti, organizzazioni e servizi.
 * 
 * Questa classe contiene il codice condiviso per l'accesso in lettura alle service entitities.
 * 
 * La specifica dei metodi è dentro govregistry-api-readops.yaml. Questa specifica è riportata dentro gli altri yaml, che per adesso sono
 * govregisty-api-backoffice.yaml e govio-api-backoffice.yaml.
 * 
 *
 */
@Component
public class ReadServiceController {
	
	@Autowired
	ServiceAssembler serviceAssembler;
	
	@Autowired
	ServiceAuthItemAssembler serviceItemAssembler;
	
	@Autowired
	ReadServiceRepository serviceRepo;
	
	@Autowired
	SecurityService authService;
	
	@Autowired
	ServiceMessages serviceMessages;
	
	@Autowired
	ApplicationConfig applicationConfig;
	
	public ResponseEntity<ServiceList> listServices(ServiceOrdering sort, Direction sortDirection, Integer limit, Long offset, String q, List<String> withRoles) {
		
		Set<String> roles = new HashSet<>(this.applicationConfig.getReadServiceRoles());
		
		if (withRoles != null) {
			roles.retainAll(withRoles);
		}
		
		Set<Long> serviceIds = this.authService.listAuthorizedServices(roles);
		
		Specification<ServiceEntity> spec;
		
		if (serviceIds == null) {
			// Non ho restrizioni
			spec = ServiceFilters.empty();
		} else if (serviceIds.isEmpty()) {
			// Nessun servizio
			spec = ServiceFilters.never();
		} else {
			// Filtra per i servizi trovati
			spec = ServiceFilters.byId(serviceIds);
		}
		if (q != null) {
			spec = spec.and(
					ServiceFilters.likeDescription(q).or(ServiceFilters.likeName(q)));
		}

		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, ServiceFilters.sort(sort,sortDirection));
		Page<ServiceEntity> services = this.serviceRepo.findAll(spec, pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		ServiceList ret = ListaUtils.buildPaginatedList(services, pageRequest.limit, curRequest, new ServiceList());
		for (ServiceEntity service : services) {
			ret.addItemsItem(this.serviceItemAssembler.toModel(service));
		}
		
		return ResponseEntity.ok(ret);
	}
	
	
	public ResponseEntity<Service> readService(Long id) {
		
		Set<Long> serviceIds = this.authService.listAuthorizedServices(this.applicationConfig.getReadServiceRoles());
		if (serviceIds != null && !serviceIds.contains(id)) {
			throw new NotAuthorizedException();
		}
		
		ServiceEntity service = this.serviceRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(this.serviceMessages.idNotFound(id)));
		
		return ResponseEntity.ok(this.serviceAssembler.toModel(service));
	}


	public ResponseEntity<Resource> downloadServiceLogo(Long id) {
		
		Set<Long> serviceIds = this.authService.listAuthorizedServices(this.applicationConfig.getReadServiceRoles());
		if (serviceIds != null && !serviceIds.contains(id)) {
			throw new NotAuthorizedException();
		}
		
		ServiceEntity service = this.serviceRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(this.serviceMessages.idNotFound(id)));
		if (service.getLogo() == null) {
			throw new ResourceNotFoundException();
		}

		byte[] ret = service.getLogo();
		ByteArrayInputStream bret = new ByteArrayInputStream(ret);
		InputStreamResource logoStream = new InputStreamResource(bret);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentLength(ret.length);
		headers.setContentType(MediaType.valueOf(service.getLogoMediaType()));
		
		return  new ResponseEntity<>(logoStream, headers, HttpStatus.OK); 
	}


	public ResponseEntity<Resource> downloadServiceLogoMiniature(Long id) {
		
		Set<Long> serviceIds = this.authService.listAuthorizedServices(this.applicationConfig.getReadServiceRoles());
		if (serviceIds != null && !serviceIds.contains(id)) {
			throw new NotAuthorizedException();
		}
		
		ServiceEntity service = this.serviceRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(this.serviceMessages.idNotFound(id)));
		if (service.getLogoMiniature() == null) {
			throw new ResourceNotFoundException();
		}

		byte[] ret = service.getLogoMiniature();
		ByteArrayInputStream bret = new ByteArrayInputStream(ret);
		InputStreamResource logoStream = new InputStreamResource(bret);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentLength(ret.length);
		headers.setContentType(MediaType.valueOf(service.getLogoMiniatureMediaType()));
		
		return  new ResponseEntity<>(logoStream, headers, HttpStatus.OK); 
	}


}
