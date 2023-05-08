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
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import it.govhub.govregistry.api.messages.RoleMessages;
import it.govhub.govregistry.api.repository.RoleAuthorizationRepository;
import it.govhub.govregistry.commons.entity.RoleAuthorizationEntity;
import it.govhub.govregistry.commons.exception.NotAuthorizedException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.readops.api.repository.ReadRoleRepository;
import it.govhub.govregistry.readops.api.repository.RoleAuthorizationFilters;
import it.govhub.security.config.GovregistryRoles;
import it.govhub.security.services.SecurityService;

@Service
public class RoleAuthorizationService {
	
	@Autowired
	RoleAuthorizationRepository authRepo;
	
	@Autowired
	ReadRoleRepository roleRepo;
	
	@Autowired
	SecurityService securityService;
	
	Logger log = LoggerFactory.getLogger(RoleAuthorizationService.class);
	
	@Transactional
	public RoleAuthorizationEntity assignAuthorization(RoleAuthorizationEntity newAuthorization) {
		
	    if ( !this.securityService.canWriteAuthorization(newAuthorization))  {
			throw new NotAuthorizedException();
		}
	    
	    newAuthorization.getUser().getAuthorizations().add(newAuthorization);
		newAuthorization = this.authRepo.save(newAuthorization);
		
		return newAuthorization;
	}
	

	@Transactional
	public RoleAuthorizationEntity updateAuthorization(RoleAuthorizationEntity srcAuth,	RoleAuthorizationEntity updateAuth) {
		this.securityService.expectAnyRole(GovregistryRoles.GOVREGISTRY_SYSADMIN, 	GovregistryRoles.GOVREGISTRY_USERS_EDITOR);

		srcAuth.setOrganizations(updateAuth.getOrganizations());
		srcAuth.setServices(updateAuth.getServices());
		srcAuth.setExpirationDate(updateAuth.getExpirationDate());

		if (!this.securityService.canWriteAuthorization(srcAuth)) {
			throw new NotAuthorizedException();
		}

		srcAuth = this.authRepo.save(srcAuth);

		return srcAuth;
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
	public Page<RoleAuthorizationEntity> listUserAuthorizations(Long id, LimitOffsetPageRequest pageRequest) {
		
		Specification<RoleAuthorizationEntity> spec = RoleAuthorizationFilters.byUser(id);
		return this.authRepo.findAll(spec, pageRequest.pageable);
	}

}
