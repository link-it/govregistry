package it.govhub.rest.backoffice.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.govhub.rest.backoffice.api.AuthorizationApi;
import it.govhub.rest.backoffice.assemblers.AuthorizationAssembler;
import it.govhub.rest.backoffice.beans.Authorization;
import it.govhub.rest.backoffice.beans.AuthorizationCreate;
import it.govhub.rest.backoffice.beans.AuthorizationList;
import it.govhub.rest.backoffice.beans.AuthorizationOrdering;
import it.govhub.rest.backoffice.entity.RoleAuthorizationEntity;
import it.govhub.rest.backoffice.entity.RoleAuthorizationEntity_;
import it.govhub.rest.backoffice.entity.RoleEntity;
import it.govhub.rest.backoffice.entity.RoleEntity_;
import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.exception.ResourceNotFoundException;
import it.govhub.rest.backoffice.exception.UnreachableException;
import it.govhub.rest.backoffice.messages.RoleMessages;
import it.govhub.rest.backoffice.repository.RoleAuthorizationFilters;
import it.govhub.rest.backoffice.repository.RoleAuthorizationRepository;
import it.govhub.rest.backoffice.repository.RoleRepository;
import it.govhub.rest.backoffice.services.RoleAuthorizationService;
import it.govhub.rest.backoffice.services.SecurityService;
import it.govhub.rest.backoffice.utils.LimitOffsetPageRequest;

@RestController
public class AuthorizationController implements AuthorizationApi {
	
	@Autowired
	public AuthorizationAssembler authAssembler;
	
	@Autowired
	private RoleAuthorizationService authService;
	
	@Autowired
	public RoleAuthorizationRepository authRepo;
	
	@Autowired
	public RoleRepository roleRepo;

	@Override
	public ResponseEntity<Authorization> assignAuthorization(Long id, AuthorizationCreate authorization) {
		
		Authorization ret =  this.authService.assignAuthorization(id, authorization);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(ret);
	}

	@Override
	public ResponseEntity<AuthorizationList> listAuthorizations(Long id, AuthorizationOrdering sort,  Direction sortDirection, Integer limit, Long offset) {
		
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
		
		AuthorizationList ret = authService.listUserAuthorizations(id, pageRequest);
		
		return ResponseEntity.ok(ret);
	}

	
	@Override
	public ResponseEntity<Void> removeAuthorization(Long id) {
		
		RoleAuthorizationEntity auth = this.authRepo.findById(id)
			.orElseThrow( () -> new ResourceNotFoundException(RoleMessages.authorizationNotFound(id)));
		
		this.authRepo.delete(auth);
		
		return ResponseEntity.status(HttpStatus.OK).build();
	}


}
