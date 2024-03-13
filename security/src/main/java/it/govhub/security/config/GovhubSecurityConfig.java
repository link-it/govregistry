/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
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
package it.govhub.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govhub.govregistry.commons.security.AccessDeniedHandlerImpl;
import it.govhub.govregistry.commons.security.PreAuthenticatedExceptionHandler;
import it.govhub.govregistry.commons.security.UnauthorizedAuthenticationEntryPoint;
import it.govhub.govregistry.commons.security.UnauthorizedBasicAuthenticationEntryPoint;
import it.govhub.security.services.GovhubUserDetailService;


@Import(SecurityExportedBeans.class)
public class GovhubSecurityConfig{

    @Value("${govshell.auth.header:Govhub-Consumer-Principal}")
    String headerAuthentication;
    
    @Value("${govhub.csp.policy:default-src 'self'}")
    String cspPolicy;
    
    @Value("${spring.mvc.servlet.path:}")
    String servletPath;

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
	
	@Bean
	public AccessDeniedHandlerImpl accessDeniedHandler() {
		return new AccessDeniedHandlerImpl();
	}

	@Bean
	public SecurityFilterChain securityFilterChainDev(HttpSecurity http, ObjectMapper jsonMapper, PreAuthenticatedExceptionHandler preAuthenticatedExceptionHandler) throws Exception {
		
		AuthenticationManager manager = this.authenticationManager(http.getSharedObject(AuthenticationConfiguration.class));
		
		RequestHeaderAuthenticationFilter filter = new RequestHeaderAuthenticationFilter();
		filter.setPrincipalRequestHeader(this.headerAuthentication);
		filter.setExceptionIfHeaderMissing(false);
		filter.setAuthenticationManager(manager);
		
		// Disabilita csrf perchè abbiamo solo richieste pre-autenticate con header
		applyAuthRules(http).csrf().disable()		
		// Autenticazione per header
		.addFilterBefore(filter, filter.getClass())																											
		.addFilterBefore(preAuthenticatedExceptionHandler, LogoutFilter.class)
		.exceptionHandling()
				// Gestisci accessDenied in modo da restituire un problem ben formato
				.accessDeniedHandler(accessDeniedHandler())																	
				// Gestisci la mancata autenticazione con un problem ben formato
				.authenticationEntryPoint(new UnauthorizedAuthenticationEntryPoint(jsonMapper))	
		.and()
			.httpBasic()
			.authenticationEntryPoint(new UnauthorizedBasicAuthenticationEntryPoint(jsonMapper));
		
		http
				// Le applicazioni di govhub non usano una sessione, nè fanno login. Arrivano solo richieste autenticate.
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER)
		.and()
			.headers()
			.xssProtection()
            .and()
            .contentSecurityPolicy(this.cspPolicy);
		
		return http.build();
	}

	@Bean
	public PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider(GovhubUserDetailService userDetailService) {
		PreAuthenticatedAuthenticationProvider ret = new PreAuthenticatedAuthenticationProvider();
		ret.setPreAuthenticatedUserDetailsService(userDetailService);
		return ret;
	}
	
	
	
	
	
	private HttpSecurity applyAuthRules(HttpSecurity http) throws Exception {
		
		http
			.authorizeRequests()
			// richieste GET Schema open-api accessibile a tutti
