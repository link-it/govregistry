package it.govhub.rest.backoffice.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import it.govhub.rest.backoffice.api.AuthorizationApi;
import it.govhub.rest.backoffice.assemblers.AuthorizationAssembler;
import it.govhub.rest.backoffice.beans.Authorization;
import it.govhub.rest.backoffice.beans.AuthorizationCreate;
import it.govhub.rest.backoffice.beans.AuthorizationList;
import it.govhub.rest.backoffice.beans.AuthorizationOrdering;
import it.govhub.rest.backoffice.entity.RoleAuthorizationEntity;
import it.govhub.rest.backoffice.entity.RoleAuthorizationEntity_;
import it.govhub.rest.backoffice.entity.RoleEntity_;
import it.govhub.rest.backoffice.exception.ResourceNotFoundException;
import it.govhub.rest.backoffice.exception.UnreachableException;
import it.govhub.rest.backoffice.messages.RoleMessages;
import it.govhub.rest.backoffice.repository.RoleAuthorizationRepository;
import it.govhub.rest.backoffice.services.RoleAuthorizationService;
import it.govhub.rest.backoffice.utils.LimitOffsetPageRequest;
import it.govhub.rest.backoffice.utils.ListaUtils;

@RestController
public class AuthorizationController implements AuthorizationApi {
	
	@Autowired
	private AuthorizationAssembler authAssembler;
	
	@Autowired
	private RoleAuthorizationService authService;
	
	@Autowired
	private RoleAuthorizationRepository authRepo;

	@Override
	public ResponseEntity<Authorization> assignAuthorization(Long id, AuthorizationCreate authorization) {
		
		RoleAuthorizationEntity newAuthorization = this.authService.assignAuthorization(id, authorization);
		
		Authorization ret = this.authAssembler.toModel(newAuthorization);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(ret);
	}

	@Override
	public ResponseEntity<AuthorizationList> listAuthorizations(Long id, Integer limit, Long offset, AuthorizationOrdering sort) {
		
		Sort orderBy = Sort.unsorted();
		if (sort != null) {
			switch(sort) {
			case ID:
				orderBy = Sort.by(Sort.Direction.ASC, RoleAuthorizationEntity_.ID);
			case ROLE_NAME:
				orderBy = Sort.by(Sort.Direction.ASC, RoleEntity_.NAME);	// TODO deve essere ROLE.ROLE_NAME
				break;
			default:
				throw new UnreachableException();
			}
		}
		
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, orderBy);
		
		Page<RoleAuthorizationEntity> auths = this.authRepo.findAll(pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		AuthorizationList ret = ListaUtils.costruisciListaPaginata(auths, curRequest, new AuthorizationList());
		
		for (RoleAuthorizationEntity auth : auths) {
			ret.addItemsItem(this.authAssembler.toModel(auth));
		}
		
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
