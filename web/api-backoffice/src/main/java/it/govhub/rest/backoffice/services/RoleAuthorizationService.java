package it.govhub.rest.backoffice.services;

import static it.govhub.rest.backoffice.utils.ListaUtils.emptyIfNull;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.govhub.rest.backoffice.beans.AuthorizationCreate;
import it.govhub.rest.backoffice.entity.OrganizationEntity;
import it.govhub.rest.backoffice.entity.RoleAuthorizationEntity;
import it.govhub.rest.backoffice.entity.RoleEntity;
import it.govhub.rest.backoffice.entity.ServiceEntity;
import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.exception.BadRequestException;
import it.govhub.rest.backoffice.exception.ResourceNotFoundException;
import it.govhub.rest.backoffice.messages.OrganizationMessages;
import it.govhub.rest.backoffice.messages.RoleMessages;
import it.govhub.rest.backoffice.messages.UserMessages;
import it.govhub.rest.backoffice.repository.OrganizationRepository;
import it.govhub.rest.backoffice.repository.RoleAuthorizationRepository;
import it.govhub.rest.backoffice.repository.RoleRepository;
import it.govhub.rest.backoffice.repository.UserRepository;

@Service
public class RoleAuthorizationService {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private OrganizationRepository orgRepo;
	
	@Autowired
	private RoleRepository roleRepo;
	
	@Autowired
	private RoleAuthorizationRepository authRepo;
	

	public RoleAuthorizationEntity assignAuthorization(Long id, AuthorizationCreate authorization) {
		
		UserEntity principal = this.userRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(UserMessages.notFound(id)));
		
		RoleEntity role = this.roleRepo.findById(authorization.getRole())
				.orElseThrow( () -> new BadRequestException(RoleMessages.notFound(id)));
		
		Set<OrganizationEntity> organizations = emptyIfNull(authorization.getOrganizations())
				.stream()
				.map( orgId -> {
						var org = this.orgRepo.findById(orgId);
						if (org.isEmpty()) {
							throw new BadRequestException(OrganizationMessages.notFound(orgId));
						}						
						return org.get();
				}).collect(Collectors.toSet());
		
		Set<ServiceEntity> services = Collections.emptySet();		// TODO
		
		RoleAuthorizationEntity newAuthorization = RoleAuthorizationEntity.builder()
			.user(principal)
			.role(role)
			.organizations(organizations)
			.services(services)
			.expirationDate(authorization.getExpirationDate())
			.build();
		
		return this.authRepo.save(newAuthorization);

	}



}
