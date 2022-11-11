package it.govhub.rest.backoffice.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;

import it.govhub.rest.backoffice.api.ProfileApi;
import it.govhub.rest.backoffice.assemblers.UserAssembler;
import it.govhub.rest.backoffice.beans.User;
import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.exception.UnreachableException;
import it.govhub.rest.backoffice.messages.SecurityMessages;
import it.govhub.rest.backoffice.repository.UserRepository;


@RestController
public class ProfileController implements ProfileApi {
	
	@Autowired
	private UserRepository userRepo;

	@Autowired
	private UserAssembler userAssembler;
	
	@Override
	public ResponseEntity<User> profile() {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		UserDetails principal = (UserDetails) authentication.getPrincipal();
		
		UserEntity user = this.userRepo.findByPrincipal(principal.getUsername())
			.orElseThrow( () -> new UnreachableException(SecurityMessages.authorizedUserNotInDb(principal.getUsername())));
		
		return ResponseEntity.ok(this.userAssembler.toModel(user));
		
	}

}
