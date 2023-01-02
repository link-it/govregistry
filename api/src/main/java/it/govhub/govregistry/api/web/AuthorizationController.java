package it.govhub.govregistry.api.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.govhub.govregistry.api.beans.AuthorizationCreate;
import it.govhub.govregistry.api.services.RoleAuthorizationService;
import it.govhub.govregistry.api.spec.AuthorizationApi;
import it.govhub.govregistry.commons.api.beans.Authorization;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.readops.api.assemblers.AuthorizationAssembler;
import it.govhub.govregistry.readops.api.repository.ReadRoleAuthorizationRepository;
import it.govhub.govregistry.readops.api.repository.ReadRoleRepository;
import it.govhub.security.services.SecurityService;


@V1RestController
public class AuthorizationController implements AuthorizationApi {
	
	@Autowired
	AuthorizationAssembler authAssembler;
	
	@Autowired
	RoleAuthorizationService authService;
	
	@Autowired
	ReadRoleAuthorizationRepository authRepo;
	
	@Autowired
	SecurityService securityService;

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


}
