package it.govhub.govregistry.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govhub.govregistry.commons.security.AccessDeniedHandlerImpl;
import it.govhub.govregistry.commons.security.GovhubUserDetailService;
import it.govhub.govregistry.commons.security.ProblemHttp403ForbiddenEntryPoint;


/**
 * Configurazione della sicurezza
 * 
 * @author nardi
 *
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig{

	@Autowired
	private ObjectMapper jsonMapper;
	
    @Value("${govshell.auth.header}")
    private String headerAuthentication;
	
	@Autowired
	public GovhubUserDetailService userDetailService;
	
    @Autowired
    private PreAuthenticatedExceptionHandler preAuthenticatedExceptionHandler;
    
    @Autowired
	private AccessDeniedHandlerImpl accessDeniedHandler;
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChainDev(HttpSecurity http) throws Exception {
		
		AuthenticationManager manager = this.authenticationManager(http.getSharedObject(AuthenticationConfiguration.class));
		
		RequestHeaderAuthenticationFilter filter = new RequestHeaderAuthenticationFilter();
		filter.setPrincipalRequestHeader(this.headerAuthentication);
		filter.setExceptionIfHeaderMissing(false);
		filter.setAuthenticationManager(manager);
		
		applyAuthRules(http).csrf().disable()																												// Disabilita csrf perchè abbiamo solo richieste pre-autenticate con header
		.addFilterBefore(filter, filter.getClass())																											// Autenticazione per header
		.addFilterBefore(preAuthenticatedExceptionHandler, LogoutFilter.class)
		.exceptionHandling()
				.accessDeniedHandler(this.accessDeniedHandler)																		// Gestisci accessDenied in modo da restituire un problem ben formato
				.authenticationEntryPoint(new ProblemHttp403ForbiddenEntryPoint(this.jsonMapper))			// Gestisci la mancata autenticazione con un problem ben formato
		.and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER)  									// Le applicazioni di govhub non usano una sessione, nè fanno login. Arrivano solo richieste autenticate.
		;
		return http.build();
	}

	
	@Bean
	public PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider() {
		PreAuthenticatedAuthenticationProvider ret = new PreAuthenticatedAuthenticationProvider();
		ret.setPreAuthenticatedUserDetailsService(userDetailService);
		return ret;
	}
	
	
	private HttpSecurity applyAuthRules(HttpSecurity http) throws Exception {
		
		http
		.authorizeRequests()
		// richieste GET Schema open-api accessibile a tutti
		.antMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll() 
		.antMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll()
		.anyRequest().authenticated()
		;
		return http;
	}
	
}
