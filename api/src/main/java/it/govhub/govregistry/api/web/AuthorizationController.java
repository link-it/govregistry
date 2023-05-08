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
package it.govhub.govregistry.api.web;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import it.govhub.govregistry.api.beans.AuthorizationCreate;
import it.govhub.govregistry.api.beans.AuthorizationUpdate;
import it.govhub.govregistry.api.beans.RoleList;
import it.govhub.govregistry.api.messages.RoleMessages;
import it.govhub.govregistry.api.repository.RoleFilters;
import it.govhub.govregistry.api.repository.ServiceRepository;
import it.govhub.govregistry.api.services.RoleAuthorizationService;
import it.govhub.govregistry.api.spec.AuthorizationApi;
import it.govhub.govregistry.commons.api.beans.Authorization;
import it.govhub.govregistry.commons.api.beans.AuthorizationList;
import it.govhub.govregistry.commons.api.beans.AuthorizationOrdering;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.RoleAuthorizationEntity;
import it.govhub.govregistry.commons.entity.RoleAuthorizationEntity_;
import it.govhub.govregistry.commons.entity.RoleEntity;
import it.govhub.govregistry.commons.entity.RoleEntity_;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.exception.BadRequestException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.exception.UnreachableException;
import it.govhub.govregistry.commons.messages.OrganizationMessages;
import it.govhub.govregistry.commons.messages.ServiceMessages;
import it.govhub.govregistry.commons.messages.UserMessages;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.govregistry.readops.api.assemblers.AuthorizationConverter;
import it.govhub.govregistry.readops.api.repository.ReadOrganizationRepository;
import it.govhub.govregistry.readops.api.repository.ReadRoleAuthorizationRepository;
import it.govhub.govregistry.readops.api.repository.ReadRoleRepository;
import it.govhub.govregistry.readops.api.repository.ReadUserRepository;
import it.govhub.security.config.GovregistryRoles;
import it.govhub.security.services.SecurityService;


@V1RestController
public class AuthorizationController implements AuthorizationApi {
	
	@Autowired
	AuthorizationConverter authAssembler;
	
	@Autowired
	RoleAuthorizationService authService;
	
	@Autowired
	ReadRoleAuthorizationRepository authRepo;
	
	@Autowired
	SecurityService securityService;
	
	@Autowired
	ReadRoleRepository roleRepo;
	
	@Autowired
	ReadUserRepository userRepo;
	
	@Autowired
	ReadOrganizationRepository orgRepo;
	
	@Autowired
	ServiceRepository serviceRepo;
	
	@Autowired
	UserMessages userMessages;
	
	@Autowired
	OrganizationMessages orgMessages;
	
	@Autowired
	ServiceMessages serviceMessages;
	
	private Logger log = LoggerFactory.getLogger(AuthorizationController.class);
	@Override
	public ResponseEntity<Authorization> assignAuthorization(Long userId, AuthorizationCreate authorization) {
		
		log.info("Assigning new authorization to user [{}]: {} )",  userId, authorization);
		
		this.securityService.expectAnyRole(GovregistryRoles.GOVREGISTRY_SYSADMIN, GovregistryRoles.GOVREGISTRY_USERS_EDITOR);
		
		UserEntity assignee = this.userRepo.findById(userId)
				.orElseThrow( () -> new ResourceNotFoundException(this.userMessages.idNotFound(userId)));
		
		RoleEntity role = this.roleRepo.findById(authorization.getRole())
				.orElseThrow( () -> new BadRequestException(RoleMessages.notFound(authorization.getRole())));
		
		// Colleziono organizzazioni e servizi
		
		Set<OrganizationEntity> organizations = retrieveOrganizations(authorization.getOrganizations());
		Set<ServiceEntity> services  = retrieveServices(authorization.getServices());
		
		RoleAuthorizationEntity newAuthorization = RoleAuthorizationEntity.builder()
			.user(assignee)
			.role(role)
			.organizations(organizations)
			.services(services)
			.expirationDate(authorization.getExpirationDate())
			.build();
		
		newAuthorization =  this.authService.assignAuthorization(newAuthorization);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(this.authAssembler.toModel(newAuthorization));
	}

	
	@Override
	public ResponseEntity<Void> removeAuthorization(Long userId, Long authId) {
		
		this.authService.removeAuthorization(authId);
		
		return ResponseEntity.status(HttpStatus.OK).build();
	}
	
	
	@Transactional
	@Override
	public ResponseEntity<AuthorizationList> listAuthorizations(Long userId, AuthorizationOrdering sort,  Direction sortDirection, Integer limit, Long offset) {
		this.log.debug("Listing Authorizations for User: {}", userId);
		
		var user = this.userRepo.findById(userId)
				.orElseThrow( () ->new ResourceNotFoundException(this.userMessages.idNotFound(userId)));
		
		this.log.debug("User [{}] authorizations: ", user.getPrincipal());
		for (var auth : user.getAuthorizations()) {
			this.log.debug(auth.getRole().getName());
		}
		
		this.securityService.expectAnyRole(GovregistryRoles.GOVREGISTRY_SYSADMIN, GovregistryRoles.GOVREGISTRY_USERS_EDITOR, GovregistryRoles.GOVREGISTRY_USERS_VIEWER);
		
		Sort orderBy;
		switch(sort) {
			case ID:
				orderBy = Sort.by(sortDirection, RoleAuthorizationEntity_.ID);
				break;
			case ROLE_NAME:
				orderBy = Sort.by(sortDirection, RoleAuthorizationEntity_.ROLE+"."+RoleEntity_.NAME);
				break;
			case UNSORTED:
				orderBy = Sort.unsorted();
				break;
			default:
				throw new UnreachableException();
		}
		
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, orderBy);
		Page<RoleAuthorizationEntity> auths = authService.listUserAuthorizations(userId, pageRequest);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		AuthorizationList ret = ListaUtils.buildPaginatedList(auths,  pageRequest.limit, curRequest, new AuthorizationList());
		
		for (RoleAuthorizationEntity auth : auths) {
			ret.addItemsItem(this.authAssembler.toModel(auth));
		}
		
		return ResponseEntity.ok(ret);
	}

	@Transactional
	@Override
	public ResponseEntity<RoleList> listRoles(Direction sortDirection,Integer limit, Long offset,  String q, String application_id) {
		this.securityService.expectAnyRole(GovregistryRoles.GOVREGISTRY_SYSADMIN, GovregistryRoles.GOVREGISTRY_USERS_EDITOR, GovregistryRoles.GOVREGISTRY_USERS_VIEWER);

		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, Sort.by(sortDirection, RoleEntity_.NAME));
		
		var spec = RoleFilters.empty();
		if (! StringUtils.isBlank(q)) {
			spec = spec.and(RoleFilters.likeRoleName(q));
		}
		if (! StringUtils.isBlank(application_id)) {
			spec = spec.and(RoleFilters.byApplicationId(application_id));
		}
		
		Page<RoleEntity> roles = this.roleRepo.findAll(null, pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		RoleList ret = ListaUtils.buildPaginatedList(roles, pageRequest.limit, curRequest, new RoleList());
		
		for (RoleEntity r : roles) {
			ret.addItemsItem(this.authAssembler.toModel(r));
		}
		
		return ResponseEntity.ok(ret);
	}


	@Override
	public ResponseEntity<Authorization> updateAuthorization(Long userId, Long authId, AuthorizationUpdate authUpdate) {
		
		log.info("Updating Authorization {} for user {}", authUpdate, authId);
		
		this.securityService.expectAnyRole(GovregistryRoles.GOVREGISTRY_SYSADMIN, GovregistryRoles.GOVREGISTRY_USERS_EDITOR);
			
		RoleAuthorizationEntity auth = this.authRepo.findById(authId)
			.orElseThrow( () -> new ResourceNotFoundException(RoleMessages.authorizationNotFound(authId)));
		
		RoleEntity role = this.roleRepo.findById(authUpdate.getRole())
				.orElseThrow( () -> new BadRequestException(RoleMessages.notFound(authUpdate.getRole())));
		 
		// Colleziono organizzazioni e servizi
		Set<OrganizationEntity> organizations = retrieveOrganizations(authUpdate.getOrganizations());
		Set<ServiceEntity> services = retrieveServices(authUpdate.getServices());
		
		RoleAuthorizationEntity newAuthorization = RoleAuthorizationEntity.builder()
			.role(role)
			.organizations(organizations)
			.services(services)
			.expirationDate(authUpdate.getExpirationDate())
			.build();
		
		auth = this.authService.updateAuthorization(auth, newAuthorization);	
		
		return ResponseEntity.ok(this.authAssembler.toModel(auth));
	}


	private Set<ServiceEntity> retrieveServices(List<Long> requestedIds) {
		Set<ServiceEntity> services  = new HashSet<>(this.serviceRepo.findAllById(requestedIds));
		Set<Long> foundIds = services.stream().map(ServiceEntity::getId).collect(Collectors.toSet());
		for (Long sid : requestedIds) {
			if (!foundIds.contains(sid)) {
				throw new BadRequestException(this.serviceMessages.idNotFound(sid));
			}			
		}
		return services;
	}


	private Set<OrganizationEntity> retrieveOrganizations(List<Long> requestedIds) {
		Set<OrganizationEntity> organizations = new HashSet<>(this.orgRepo.findAllById(requestedIds));
		Set<Long> orgIds = organizations.stream().map(OrganizationEntity::getId).collect(Collectors.toSet());
		for (Long oid: requestedIds) {
			if (!orgIds.contains(oid)) {
				throw new BadRequestException(this.orgMessages.idNotFound(oid));
			}
		}
		return organizations;
	}
	
	


}



















