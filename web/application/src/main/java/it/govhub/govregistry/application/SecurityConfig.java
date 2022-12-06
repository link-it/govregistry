package it.govhub.govregistry.application;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govhub.govregistry.api.config.AccessDeniedHandlerImpl;
import it.govhub.govregistry.api.config.SecurityConstants;
import it.govhub.govregistry.api.security.GovhubUserDetailService;
import it.govhub.govregistry.api.security.PreAuthenticatedExceptionHandler;
import it.govhub.govregistry.api.security.ProblemHttp403ForbiddenEntryPoint;


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
	
    @Value("${auth.header}")
    private String headerAuthentication;
	
	@Autowired
	public GovhubUserDetailService userDetailService;
	
    @Autowired
    private PreAuthenticatedExceptionHandler preAuthenticatedExceptionHandler;
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		return new AccessDeniedHandlerImpl(this.jsonMapper);
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
				.accessDeniedHandler(this.accessDeniedHandler())																		// Gestisci accessDenied in modo da restituire un problem ben formato
				.authenticationEntryPoint(new ProblemHttp403ForbiddenEntryPoint(this.jsonMapper));		// Gestisci la mancata autenticazione con un problem ben formato
		return http.build();
	}

	
	@Bean
	public CookieSerializer cookieSerializer() {
		DefaultCookieSerializer serializer = new DefaultCookieSerializer();
		serializer.setCookieName(SecurityConstants.JSESSIONID_NAME); 
		serializer.setCookiePath("/"); 
		serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$"); 
		return serializer;
	}


	@Bean
	public PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider() {
		PreAuthenticatedAuthenticationProvider ret = new PreAuthenticatedAuthenticationProvider();
		ret.setPreAuthenticatedUserDetailsService(userDetailService);
		return ret;
	}
	
	
	public class DefaultLogoutSuccessHandler implements LogoutSuccessHandler {
	    @Override
	    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
	            response.setStatus(HttpServletResponse.SC_OK);
	    }
	}
	
	
	private HttpSecurity applyAuthRules(HttpSecurity http) throws Exception {
		
		http
		.authorizeRequests()
		// richieste GET Schema open-api accessibile a tutti
		.antMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll() 
		.antMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll()
		.anyRequest().authenticated()
		.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)  // Creazione della sessione in caso non ci sia
		.and().logout().deleteCookies(SecurityConstants.JSESSIONID_NAME).invalidateHttpSession(true).logoutSuccessHandler(new DefaultLogoutSuccessHandler()) // Gestione Logout
		;
		return http;
	}
	
}
