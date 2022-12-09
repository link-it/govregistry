package it.govhub.govregistry.application.services;

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

import it.govhub.govregistry.api.assemblers.AuthorizationAssembler;
import it.govhub.govregistry.api.beans.Authorization;
import it.govhub.govregistry.api.beans.AuthorizationCreate;
import it.govhub.govregistry.api.beans.AuthorizationList;
import it.govhub.govregistry.api.entity.OrganizationEntity;
import it.govhub.govregistry.api.entity.RoleAuthorizationEntity;
import it.govhub.govregistry.api.entity.RoleEntity;
import it.govhub.govregistry.api.entity.ServiceEntity;
import it.govhub.govregistry.api.entity.UserEntity;
import it.govhub.govregistry.api.exception.BadRequestException;
import it.govhub.govregistry.api.exception.NotAuthorizedException;
import it.govhub.govregistry.api.exception.ResourceNotFoundException;
import it.govhub.govregistry.api.messages.OrganizationMessages;
import it.govhub.govregistry.api.messages.RoleMessages;
import it.govhub.govregistry.api.messages.ServiceMessages;
import it.govhub.govregistry.api.messages.UserMessages;
import it.govhub.govregistry.api.repository.OrganizationRepository;
import it.govhub.govregistry.api.repository.RoleAuthorizationFilters;
import it.govhub.govregistry.api.repository.RoleAuthorizationRepository;
import it.govhub.govregistry.api.repository.RoleRepository;
import it.govhub.govregistry.api.repository.ServiceRepository;
import it.govhub.govregistry.api.repository.UserRepository;
import it.govhub.govregistry.api.services.SecurityService;
import it.govhub.govregistry.api.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.api.utils.ListaUtils;

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

	@Transactional
	public Authorization assignAuthorization(Long userId, AuthorizationCreate authorization) {
		
		UserEntity assignee = this.userRepo.findById(userId)
				.orElseThrow( () -> new ResourceNotFoundException(UserMessages.notFound(userId)));
		
		RoleEntity role = this.roleRepo.findById(authorization.getRole())
				.orElseThrow( () -> new BadRequestException(RoleMessages.notFound(userId)));
		
		UserEntity principal = SecurityService.getPrincipal();
		
		// In quanto principal posso assegnare solo i ruoli che sono fra i mei assignable_roles
		// Posso assegnarli se il principal ha una autorizzazione con un ruolo che ha fra gli assignable_roles il ruolo da assegnare
		// e la cui expiration_date è superiore a quella del ruolo da assegnare
		// Devo cercare una RoleAuthorizationEntity che me lo fa fare
		
		Specification<RoleAuthorizationEntity> spec = RoleAuthorizationFilters.byUser(principal.getId())
				.and(RoleAuthorizationFilters.byAssignableRole(authorization.getRole()))
				.and(RoleAuthorizationFilters.onServices(authorization.getServices()))
				.and(RoleAuthorizationFilters.onOrganizations(authorization.getOrganizations()))
				.and(RoleAuthorizationFilters.expiresAfter(authorization.getExpirationDate()));
		
		Specification<RoleAuthorizationEntity> adminSpec = RoleAuthorizationFilters.byAdmin(principal.getId());
		
		if (this.authRepo.findAll(spec.or(adminSpec)).isEmpty()) {
			throw new NotAuthorizedException();
		}

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
		
		newAuthorization = this.authRepo.save(newAuthorization);
		
		return this.authAssembler.toModel(newAuthorization);
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
