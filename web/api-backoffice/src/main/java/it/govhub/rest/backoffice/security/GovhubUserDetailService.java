package it.govhub.rest.backoffice.security;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import it.govhub.rest.backoffice.config.Caches;
import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.exception.ResourceNotFoundException;
import it.govhub.rest.backoffice.repository.UserRepository;

//@Service
public class GovhubUserDetailService implements UserDetailsService {
	
	@Autowired
	private UserRepository userRepo;


	@Override
	@Transactional
	@Cacheable(Caches.PRINCIPALS)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		UserEntity user = this.userRepo.findAndPreloadByPrincipal(username)
				.orElseThrow( () -> new ResourceNotFoundException("Credenziali non Valide"));
		
		return new GovhubPrincipal(user);
	}

}
