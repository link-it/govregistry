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


/**
 * Configurazione della sicurezza
 * 
 *
 */
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
	public SecurityFilterChain securityFilterChainDev(HttpSecurity http, ObjectMapper jsonMapper, PreAuthenticatedExceptionHandler preAuthenticatedExceptionHandler, AccessDeniedHandlerImpl accessDeniedHandler) throws Exception {
		
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
				.accessDeniedHandler(accessDeniedHandler)																	
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
			.xssProtection();
         //   .and()
         // Politica di CSP più restrittiva. https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP
         // Anche le immagini dal gravatar
        //.contentSecurityPolicy(this.cspPolicy);
		
		return http.build();
	}

	/**
	 * Impstiamo il servizio per caricare l'utente a partire dallo header.
	 */
	@Bean
	public PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider(GovhubUserDetailService userDetailService) {
		PreAuthenticatedAuthenticationProvider ret = new PreAuthenticatedAuthenticationProvider();
		ret.setPreAuthenticatedUserDetailsService(userDetailService);
		return ret;
	}
	
	
	/**
	 * Registriamo lo UserDetailService per essere chiamato in caso di autenticazione basic
	 */
	
	/*@Bean
	public GovhubUserDetailService userDetailService() {
		
	}*/
	
	
	private HttpSecurity applyAuthRules(HttpSecurity http) throws Exception {
		
		http
			.authorizeRequests()
			// richieste GET Schema open-api accessibile a tutti
			.antMatchers(HttpMethod.GET, servletPath+"/swagger-ui/**").permitAll() 
			.antMatchers(HttpMethod.GET, servletPath+"/v3/api-docs/**").permitAll()
			.antMatchers(HttpMethod.GET, servletPath+"/govregistry-api-backoffice.yaml").permitAll()
			.antMatchers(HttpMethod.GET, servletPath+"/govio-api-backoffice.yaml").permitAll()
			.antMatchers(HttpMethod.GET, servletPath+"/govhub-api-commons.yaml").permitAll()
			.anyRequest().authenticated();
		
		return http;
	}
	
}
