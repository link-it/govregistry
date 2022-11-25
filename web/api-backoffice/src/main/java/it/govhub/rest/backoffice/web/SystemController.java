package it.govhub.rest.backoffice.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import it.govhub.rest.backoffice.api.SystemApi;
import it.govhub.rest.backoffice.assemblers.UserAssembler;
import it.govhub.rest.backoffice.beans.Problem;
import it.govhub.rest.backoffice.beans.Profile;
import it.govhub.rest.backoffice.exception.RestResponseEntityExceptionHandler;
import it.govhub.rest.backoffice.security.GovhubPrincipal;
import it.govhub.rest.backoffice.services.UserService;

@RestController
public class SystemController implements SystemApi {
	
	@Autowired
	private UserService userService;
	
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
		
		GovhubPrincipal principal = (GovhubPrincipal) authentication.getPrincipal();
		
		return ResponseEntity.ok(this.userAssembler.toProfileModel(principal.getUser()));
		
	}

}
