package it.govhub.rest.backoffice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.exception.ResourceNotFoundException;
import it.govhub.rest.backoffice.messages.UserMessages;
import it.govhub.rest.backoffice.repository.UserRepository;

//@Service
public class GovhubUserDetailService implements UserDetailsService {
	
	@Autowired
	private UserRepository userRepo;


	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		UserEntity user = this.userRepo.findByPrincipal(username)
				.orElseThrow( () -> new ResourceNotFoundException(UserMessages.notFound(username)));
		
		return new GovhubPrincipal(user);
	}

}
