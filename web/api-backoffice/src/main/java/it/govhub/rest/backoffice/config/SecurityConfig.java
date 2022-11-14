package it.govhub.rest.backoffice.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govhub.rest.backoffice.exception.RestResponseEntityExceptionHandler;
import it.govhub.rest.backoffice.exception.UnreachableException;


/**
 * Configurazione della sicurezza
 * 
 * @author nardi
 *
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Value("${spring.security.userPropertyFile}")
	String fileUtenze;
	
	@Autowired
	private ObjectMapper jsonMapper;
	
	public static final String REALM_NAME = "govhub";
	public static final String JSESSIONID_NAME = "GOVHUB-JSESSIONID";
	public static final String RUOLO_GOVHUB_SYSADMIN = "govhub_sysadmin"; // Accesso a tutte le risorse
	public static final String RUOLO_GOVHUB_USERS_EDITOR = "govhub_users_editor";
	public static final String RUOLO_GOVHUB_USERS_VIEWER = "govhub_users_viewer";
	public static final String RUOLO_GOVHUB_USER = "govhub_user";
	public static final String RUOLO_GOVHUB_ORGANIZATIONS_EDITOR = "govhub_organizations_editor";
	public static final String RUOLO_GOVHUB_ORGANIZATIONS_VIEWER = "govhub_organizations_viewer";

	// impostarli nel componente jee utilizzando la funzione mappableAuthorities al posto di mappableRoles che aggiunge il prefisso 'ROLE_' ad ogni ruolo
	public static final Set<String> ruoliConsentiti = Set.of
			( 
				RUOLO_GOVHUB_SYSADMIN,
				RUOLO_GOVHUB_USERS_EDITOR,
				RUOLO_GOVHUB_USERS_VIEWER,
				RUOLO_GOVHUB_USER,
				RUOLO_GOVHUB_ORGANIZATIONS_EDITOR,
				RUOLO_GOVHUB_ORGANIZATIONS_VIEWER
			);
	
	
	@Bean
	public SecurityFilterChain securityFilterChainDev(HttpSecurity http) throws Exception {
		applyAuthRules(http).authorizeRequests()
			.and().httpBasic().authenticationEntryPoint(new BasicAuthenticationEntryPoint(jsonMapper))
			.and().exceptionHandling().accessDeniedHandler(this.accessDeniedHandler());  // Gestione degli errori di autenticazione, con entry point che personalizza la risposta inviata 
		return http.build();
	}

	private HttpSecurity applyAuthRules(HttpSecurity http) throws Exception {
		
		http
		.authorizeRequests()
		.antMatchers(HttpMethod.POST, "/users").hasAnyRole(RUOLO_GOVHUB_SYSADMIN, RUOLO_GOVHUB_USERS_EDITOR)
		.antMatchers(HttpMethod.PATCH, "/users/**").hasAnyRole(RUOLO_GOVHUB_SYSADMIN, RUOLO_GOVHUB_USERS_EDITOR)
		.antMatchers(HttpMethod.GET, "/users/**").hasAnyRole(RUOLO_GOVHUB_SYSADMIN, RUOLO_GOVHUB_USERS_EDITOR, RUOLO_GOVHUB_USERS_VIEWER)
		
		.antMatchers(HttpMethod.GET, "/organizations/**").hasAnyRole(RUOLO_GOVHUB_SYSADMIN, RUOLO_GOVHUB_ORGANIZATIONS_EDITOR, RUOLO_GOVHUB_ORGANIZATIONS_VIEWER)
		.antMatchers(HttpMethod.POST, "/organizations").hasAnyRole(RUOLO_GOVHUB_SYSADMIN, RUOLO_GOVHUB_ORGANIZATIONS_EDITOR)
		.antMatchers(HttpMethod.PATCH, "/organizations/**").hasAnyRole(RUOLO_GOVHUB_SYSADMIN, RUOLO_GOVHUB_ORGANIZATIONS_EDITOR)
		
		.antMatchers(HttpMethod.GET, "/profile").hasAnyRole(ruoliConsentiti.toArray(new String[0]))
		// richieste GET Schema open-api accessibile a tutti
		.antMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll() 
		.antMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll()
		.antMatchers(HttpMethod.GET, "/status").authenticated()
		.antMatchers(HttpMethod.GET, "/error").authenticated()
		.anyRequest().denyAll()
		.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)  // Creazione della sessione in caso non ci sia
		.and().logout().deleteCookies(JSESSIONID_NAME).invalidateHttpSession(true).logoutSuccessHandler(new DefaultLogoutSuccessHandler()) // Gestione Logout
		;
		return http;
	}
	
	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		return new AccessDeniedHandlerImpl(this.jsonMapper);
	}
	
	
	@Bean
	public CookieSerializer cookieSerializer() {
		DefaultCookieSerializer serializer = new DefaultCookieSerializer();
		serializer.setCookieName(JSESSIONID_NAME); 
		serializer.setCookiePath("/"); 
		serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$"); 
		return serializer;
	}
	
	
	@Bean
    public InMemoryUserDetailsManager userDetailsService() throws IOException {
		Properties properties = PropertiesLoaderUtils.loadAllProperties(this.fileUtenze);
		if(properties.size() == 0) {
			try (FileInputStream fs = new FileInputStream(this.fileUtenze)) {
				properties.load(fs);
				if(properties.size() == 0) {
					throw new IOException("File " + this.fileUtenze + " non esistente o vuoto");
				}
			}
		}
		return new InMemoryUserDetailsManager(properties);
	}
	
	
	public class DefaultLogoutSuccessHandler implements LogoutSuccessHandler {

	    @Override
	    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
	            response.setStatus(HttpServletResponse.SC_OK);
	    }

	}
	
	
	public class BasicAuthenticationEntryPoint extends org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint {
		
		ObjectMapper jsonMapper;
		
		public BasicAuthenticationEntryPoint(ObjectMapper jsonMapper) {
			this.setRealmName(REALM_NAME);
			this.jsonMapper = jsonMapper;
		}

		@Override
		public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
			 
			AuthenticationProblem problem = new AuthenticationProblem();
			problem.status = HttpStatus.UNAUTHORIZED.value();
			problem.title = HttpStatus.UNAUTHORIZED.getReasonPhrase();
			problem.detail = authException.getMessage();
			
			// imposto il content-type della risposta
			response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
			response.setStatus(problem.status);
			
			ServletOutputStream outputStream = null;
			try{
				problem.instance = new URI(RestResponseEntityExceptionHandler.problemTypes.get(HttpStatus.UNAUTHORIZED));
				outputStream = response.getOutputStream();
				this.jsonMapper.writeValue(outputStream, problem);
				outputStream.flush();
			} catch(Exception e) {
				throw new UnreachableException();
			}
			
		}
		
	}
}
