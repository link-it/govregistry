package it.govhub.security.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.exception.NotAuthorizedException;
import it.govhub.security.beans.GovhubPrincipal;
import it.govhub.security.cache.Caches;
import it.govhub.security.repository.SecurityUserRepository;

@Service
public class GovhubUserDetailService implements UserDetailsService, AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
	
	@Autowired
	private SecurityUserRepository userRepo;

	@Override
	@Cacheable(Caches.PRINCIPALS)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		UserEntity user = this.userRepo.findAndPreloadByPrincipal(username)
				.orElseThrow( () -> new NotAuthorizedException("Credenziali non Valide"));
		
		return new GovhubPrincipal(user);
	}


	@Override
	@Cacheable(cacheNames = Caches.PRINCIPALS, key = "#token.getPrincipal")
	public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
		return this.loadUserByUsername((String)token.getPrincipal());
	}

}
