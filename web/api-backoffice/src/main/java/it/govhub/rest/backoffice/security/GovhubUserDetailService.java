package it.govhub.rest.backoffice.security;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.exception.ResourceNotFoundException;
import it.govhub.rest.backoffice.repository.UserRepository;

//@Service
public class GovhubUserDetailService implements UserDetailsService {
	
	@Autowired
	private UserRepository userRepo;


	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		UserEntity user = this.userRepo.findByPrincipal(username)
				.orElseThrow( () -> new ResourceNotFoundException("Credenziali non Valide"));
		
		// Precarico tutte le relazioni lazy necessarie poi alla validazione
		for(var auth : user.getAuthorizations()) {
			auth.getServices();
			auth.getOrganizations();
		}
		
		return new GovhubPrincipal(user);
	}

}
