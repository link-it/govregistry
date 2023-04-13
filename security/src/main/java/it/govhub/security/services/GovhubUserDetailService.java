/*
 * GovRegistry - Registries manager for GovHub
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govhub.security.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.messages.UserMessages;
import it.govhub.security.beans.GovhubPrincipal;
import it.govhub.security.cache.Caches;
import it.govhub.security.repository.SecurityUserRepository;

@Service
public class GovhubUserDetailService implements UserDetailsService, AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
	
	@Autowired
	SecurityUserRepository userRepo;
	
	@Autowired
	UserMessages userMessages;
	
	Logger log = LoggerFactory.getLogger(GovhubUserDetailService.class);

	@Override
	@Cacheable(Caches.PRINCIPALS)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		log.debug("Loading principal [{}] from database...", username);
		
		UserEntity user = this.userRepo.findAndPreloadByPrincipal(username)
				.orElseThrow( () -> new UsernameNotFoundException(this.userMessages.principalNotFound(username)));
		
		return new GovhubPrincipal(user);
	}


	@Override
	@Cacheable(cacheNames = Caches.PRINCIPALS, key = "#token.getPrincipal")
	public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
		return this.loadUserByUsername((String)token.getPrincipal());
	}

}
