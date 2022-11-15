package it.govhub.rest.backoffice.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;

import it.govhub.rest.backoffice.api.SystemApi;
import it.govhub.rest.backoffice.assemblers.UserAssembler;
import it.govhub.rest.backoffice.beans.Problem;
import it.govhub.rest.backoffice.beans.Profile;
import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.exception.RestResponseEntityExceptionHandler;
import it.govhub.rest.backoffice.exception.UnreachableException;
import it.govhub.rest.backoffice.messages.SecurityMessages;
import it.govhub.rest.backoffice.repository.UserRepository;

@RestController
public class StatusController implements SystemApi {
	
	@Autowired
	private UserRepository userRepo;

	@Autowired
	private UserAssembler userAssembler;
	

	@Override
	public ResponseEntity<Problem> status() {
		
		Problem ret =  RestResponseEntityExceptionHandler.buildProblem(HttpStatus.OK,"System is working correctly");
		ret.removeLinks();
		
		return ResponseEntity.ok(ret);
	}
	

	@Override
	public ResponseEntity<Profile> profile() {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		UserDetails principal = (UserDetails) authentication.getPrincipal();
		
		UserEntity user = this.userRepo.findByPrincipal(principal.getUsername())
			.orElseThrow( () -> new UnreachableException(SecurityMessages.authorizedUserNotInDb(principal.getUsername())));
		
		return (ResponseEntity<Profile>) ResponseEntity.ok(); //this.userAssembler.toModel(user));
		
	}

}
