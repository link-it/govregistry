package it.govhub.rest.backoffice.security;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import it.govhub.rest.backoffice.config.Caches;
import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.exception.NotAuthorizedException;
import it.govhub.rest.backoffice.repository.UserRepository;

@Service
public class GovhubUserDetailService implements UserDetailsService, AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
	
	@Autowired
	private UserRepository userRepo;

	
	@Override
	@Transactional
	@Cacheable(Caches.PRINCIPALS)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		UserEntity user = this.userRepo.findAndPreloadByPrincipal(username)
				.orElseThrow( () -> new NotAuthorizedException("Credenziali non Valide"));
		
		return new GovhubPrincipal(user);
	}


	@Override
	public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
		return this.loadUserByUsername((String)token.getPrincipal());
	}

}
