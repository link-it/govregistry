package it.govhub.govregistry.api.services;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import it.govhub.govregistry.api.beans.AuthorizationCreate;
import it.govhub.govregistry.api.messages.OrganizationMessages;
import it.govhub.govregistry.api.messages.RoleMessages;
import it.govhub.govregistry.api.messages.ServiceMessages;
import it.govhub.govregistry.api.messages.UserMessages;
import it.govhub.govregistry.api.repository.RoleAuthorizationFilters;
import it.govhub.govregistry.commons.api.beans.Authorization;
import it.govhub.govregistry.commons.api.beans.AuthorizationList;
import it.govhub.govregistry.commons.assemblers.AuthorizationAssembler;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.RoleAuthorizationEntity;
import it.govhub.govregistry.commons.entity.RoleEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.exception.BadRequestException;
import it.govhub.govregistry.commons.exception.NotAuthorizedException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.repository.OrganizationRepository;
import it.govhub.govregistry.commons.repository.RoleAuthorizationRepository;
import it.govhub.govregistry.commons.repository.RoleRepository;
import it.govhub.govregistry.commons.repository.ServiceRepository;
import it.govhub.govregistry.commons.repository.UserRepository;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.security.config.GovregistryRoles;
import it.govhub.security.services.SecurityService;

@Service
public class RoleAuthorizationService {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private OrganizationRepository orgRepo;
	
	@Autowired
	private RoleRepository roleRepo;
	
	@Autowired
	private ServiceRepository serviceRepo;
	
	@Autowired
	private RoleAuthorizationRepository authRepo;
	
	@Autowired
	private AuthorizationAssembler authAssembler;
	
	@Autowired
	SecurityService securityService;

	@Transactional
	public Authorization assignAuthorization(Long userId, AuthorizationCreate authorization) {
		
		this.securityService.expectAnyRole(GovregistryRoles.RUOLO_GOVHUB_SYSADMIN, GovregistryRoles.RUOLO_GOVREGISTRY_USERS_EDITOR);
		
		UserEntity assignee = this.userRepo.findById(userId)
				.orElseThrow( () -> new ResourceNotFoundException(UserMessages.notFound(userId)));
		
		RoleEntity role = this.roleRepo.findById(authorization.getRole())
				.orElseThrow( () -> new BadRequestException(RoleMessages.notFound(userId)));
		
		// Colleziono organizzazioni e servizi
		
		Set<OrganizationEntity> organizations = new HashSet<>(this.orgRepo.findAllById(authorization.getOrganizations()));
		Set<Long> orgIds = organizations.stream().map(OrganizationEntity::getId).collect(Collectors.toSet());
		
		for (Long oid: authorization.getOrganizations()) {
			if (!orgIds.contains(oid)) {
				throw new BadRequestException(OrganizationMessages.notFound(oid));
			}
		}
	
		Set<ServiceEntity> services  = new HashSet<>(this.serviceRepo.findAllById(authorization.getServices()));
		Set<Long> foundIds = services.stream().map(ServiceEntity::getId).collect(Collectors.toSet());
		
		for (Long sid : authorization.getServices()) {
			if (!foundIds.contains(sid)) {
				throw new BadRequestException(ServiceMessages.notFound(sid));
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
		
		newAuthorization = this.authRepo.save(newAuthorization);
		
		return this.authAssembler.toModel(newAuthorization);
	}
	
	
	@Transactional
	public void removeAuthorization(Long authId) {
		this.securityService.expectAnyRole(GovregistryRoles.RUOLO_GOVHUB_SYSADMIN, GovregistryRoles.RUOLO_GOVREGISTRY_USERS_EDITOR);
		
		RoleAuthorizationEntity auth = this.authRepo.findById(authId)
			.orElseThrow( () -> new ResourceNotFoundException(RoleMessages.authorizationNotFound(authId)));
		
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
		
		AuthorizationList ret = ListaUtils.costruisciListaPaginata(auths,  pageRequest.limit, curRequest, new AuthorizationList());
		
		for (RoleAuthorizationEntity auth : auths) {
			ret.addItemsItem(this.authAssembler.toModel(auth));
		}
		return ret;
	}
	

}
