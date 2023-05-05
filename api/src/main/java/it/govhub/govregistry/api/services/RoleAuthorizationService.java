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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import it.govhub.govregistry.api.beans.AuthorizationCreate;
import it.govhub.govregistry.api.messages.RoleMessages;
import it.govhub.govregistry.api.repository.RoleAuthorizationRepository;
import it.govhub.govregistry.api.repository.ServiceRepository;
import it.govhub.govregistry.api.repository.UserRepository;
import it.govhub.govregistry.commons.api.beans.Authorization;
import it.govhub.govregistry.commons.api.beans.AuthorizationList;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.RoleAuthorizationEntity;
import it.govhub.govregistry.commons.entity.RoleEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.exception.BadRequestException;
import it.govhub.govregistry.commons.exception.NotAuthorizedException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.messages.OrganizationMessages;
import it.govhub.govregistry.commons.messages.ServiceMessages;
import it.govhub.govregistry.commons.messages.UserMessages;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.govregistry.readops.api.assemblers.AuthorizationConverter;
import it.govhub.govregistry.readops.api.repository.ReadOrganizationRepository;
import it.govhub.govregistry.readops.api.repository.ReadRoleRepository;
import it.govhub.govregistry.readops.api.repository.RoleAuthorizationFilters;
import it.govhub.security.config.GovregistryRoles;
import it.govhub.security.services.SecurityService;

@Service
public class RoleAuthorizationService {
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	ReadOrganizationRepository orgRepo;
	
	@Autowired
	ServiceRepository serviceRepo;
	
	@Autowired
	RoleAuthorizationRepository authRepo;
	
	@Autowired
	ReadRoleRepository roleRepo;
	
	@Autowired
	AuthorizationConverter authAssembler;
	
	@Autowired
	SecurityService securityService;
	
	@Autowired
	UserMessages userMessages;
	
	@Autowired
	OrganizationMessages orgMessages;
	
	@Autowired
	ServiceMessages serviceMessages;
	
	Logger log = LoggerFactory.getLogger(RoleAuthorizationService.class);
	
	@Transactional
	public Authorization assignAuthorization(Long userId, AuthorizationCreate authorization) {
		
		log.info("Assigning new authorization to user [{}]: {} )",  userId, authorization);
		
		this.securityService.expectAnyRole(GovregistryRoles.GOVREGISTRY_SYSADMIN, GovregistryRoles.GOVREGISTRY_USERS_EDITOR);
		
		UserEntity assignee = this.userRepo.findById(userId)
				.orElseThrow( () -> new ResourceNotFoundException(this.userMessages.idNotFound(userId)));
		
		RoleEntity role = this.roleRepo.findById(authorization.getRole())
				.orElseThrow( () -> new BadRequestException(RoleMessages.notFound(authorization.getRole())));
		
		// Colleziono organizzazioni e servizi
		
		Set<OrganizationEntity> organizations = new HashSet<>(this.orgRepo.findAllById(authorization.getOrganizations()));
		Set<Long> orgIds = organizations.stream().map(OrganizationEntity::getId).collect(Collectors.toSet());
		
		for (Long oid: authorization.getOrganizations()) {
			if (!orgIds.contains(oid)) {
				throw new BadRequestException(this.orgMessages.idNotFound(oid));
			}
		}
	
		Set<ServiceEntity> services  = new HashSet<>(this.serviceRepo.findAllById(authorization.getServices()));
		Set<Long> foundIds = services.stream().map(ServiceEntity::getId).collect(Collectors.toSet());
		
		for (Long sid : authorization.getServices()) {
			if (!foundIds.contains(sid)) {
				throw new BadRequestException(this.serviceMessages.idNotFound(sid));
			}			
		}
		
		RoleAuthorizationEntity newAuthorization = RoleAuthorizationEntity.builder()
			.user(assignee)
			.role(role)
			.organizations(organizations)
			.services(services)
			.expirationDate(authorization.getExpirationDate())
			.build();
		
		// Autorizzo
		
	    if ( !this.securityService.canWriteAuthorization(newAuthorization))  {
			throw new NotAuthorizedException();
		}
	    
	    assignee.getAuthorizations().add(newAuthorization);
		newAuthorization = this.authRepo.save(newAuthorization);
		
		return this.authAssembler.toModel(newAuthorization);
	}
	
	
	@Transactional
	public void removeAuthorization(Long authId) {
		this.securityService.expectAnyRole(GovregistryRoles.GOVREGISTRY_SYSADMIN, GovregistryRoles.GOVREGISTRY_USERS_EDITOR);
		
		RoleAuthorizationEntity auth = this.authRepo.findById(authId)
			.orElseThrow( () -> new ResourceNotFoundException(RoleMessages.authorizationNotFound(authId)));
		
		log.info("Removing Authorization [{}] from user [{}]", auth.getId(), auth.getUser().getPrincipal());
		
	    if ( !this.securityService.canWriteAuthorization(auth))  {
			throw new NotAuthorizedException();
		}
		
		this.authRepo.delete(auth);
	}
	
	
	@Transactional
	public AuthorizationList listUserAuthorizations(Long id, LimitOffsetPageRequest pageRequest) {
		
		Specification<RoleAuthorizationEntity> spec = RoleAuthorizationFilters.byUser(id);
		
		Page<RoleAuthorizationEntity> auths = this.authRepo.findAll(spec, pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		AuthorizationList ret = ListaUtils.buildPaginatedList(auths,  pageRequest.limit, curRequest, new AuthorizationList());
		
		for (RoleAuthorizationEntity auth : auths) {
			ret.addItemsItem(this.authAssembler.toModel(auth));
		}
		return ret;
	}

	
}
