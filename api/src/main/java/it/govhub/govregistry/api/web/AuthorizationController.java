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

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import it.govhub.govregistry.api.beans.AuthorizationCreate;
import it.govhub.govregistry.api.beans.RoleList;
import it.govhub.govregistry.api.services.RoleAuthorizationService;
import it.govhub.govregistry.api.spec.AuthorizationApi;
import it.govhub.govregistry.commons.api.beans.Authorization;
import it.govhub.govregistry.commons.api.beans.AuthorizationList;
import it.govhub.govregistry.commons.api.beans.AuthorizationOrdering;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.entity.RoleAuthorizationEntity_;
import it.govhub.govregistry.commons.entity.RoleEntity;
import it.govhub.govregistry.commons.entity.RoleEntity_;
import it.govhub.govregistry.commons.exception.UnreachableException;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.govregistry.readops.api.assemblers.AuthorizationConverter;
import it.govhub.govregistry.readops.api.repository.ReadRoleAuthorizationRepository;
import it.govhub.govregistry.readops.api.repository.ReadRoleRepository;
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

	@Override
	public ResponseEntity<Authorization> assignAuthorization(Long id, AuthorizationCreate authorizationCreate) {
		
		Authorization ret =  this.authService.assignAuthorization(id, authorizationCreate);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(ret);
	}

	
	@Override
	public ResponseEntity<Void> removeAuthorization(Long authId) {
		
		this.authService.removeAuthorization(authId);
		
		return ResponseEntity.status(HttpStatus.OK).build();
	}
	
	
	@Override
	public ResponseEntity<AuthorizationList> listAuthorizations(Long userId, AuthorizationOrdering sort,  Direction sortDirection, Integer limit, Long offset) {
		
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
		
		AuthorizationList ret = authService.listUserAuthorizations(userId, pageRequest);
		
		return ResponseEntity.ok(ret);
	}

	@Transactional
	@Override
	public ResponseEntity<RoleList> listRoles(Direction sortDirection,Integer limit, Long offset, String q) {
		this.securityService.expectAnyRole(GovregistryRoles.GOVREGISTRY_SYSADMIN, GovregistryRoles.GOVREGISTRY_USERS_EDITOR, GovregistryRoles.GOVREGISTRY_USERS_VIEWER);

		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, Sort.by(sortDirection, RoleEntity_.NAME));
		
		Page<RoleEntity> roles = this.roleRepo.findAll(null, pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		RoleList ret = ListaUtils.buildPaginatedList(roles, pageRequest.limit, curRequest, new RoleList());
		
		for (RoleEntity r : roles) {
			ret.addItemsItem(this.authAssembler.toModel(r));
		}
		
		return ResponseEntity.ok(ret);
	}


}
