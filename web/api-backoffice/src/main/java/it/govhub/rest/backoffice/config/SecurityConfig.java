package it.govhub.rest.backoffice.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

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
	
	public static final String REALM_NAME = "govhub";
	public static final String JSESSIONID_NAME = "GOVHUB-JSESSIONID";
	public static final String RUOLO_GOVHUB_SYSADMIN = "govhub_sysadmin"; // Accesso a tutte le risorse

	// impostarli nel componente jee utilizzando la funzione mappableAuthorities al posto di mappableRoles che aggiunge il prefisso 'ROLE_' ad ogni ruolo
	public static final Set<String> ruoliConsentiti = Set.of
			( 
				RUOLO_GOVHUB_SYSADMIN
			);
	
	
	@Bean
	public SecurityFilterChain securityFilterChainDev(HttpSecurity http) throws Exception {
		applyAuthRules(http).authorizeRequests()
			.and().httpBasic().authenticationEntryPoint(new BasicAuthenticationEntryPoint())
			.and().exceptionHandling();  // Gestione degli errori di autenticazione, con entry point che personalizza la risposta inviata 
		return http.build();
	}

	private HttpSecurity applyAuthRules(HttpSecurity http) throws Exception {
		http.csrf().disable()
		.authorizeRequests()
		.anyRequest().authenticated()
		.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)  // Creazione della sessione in caso non ci sia
		.and().logout().deleteCookies(JSESSIONID_NAME).invalidateHttpSession(true).logoutSuccessHandler(new DefaultLogoutSuccessHandler()) // Gestione Logout
		;
		return http;
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
    public InMemoryUserDetailsManager userDetailsService() throws Exception {
		Properties properties = PropertiesLoaderUtils.loadAllProperties(this.fileUtenze);
		if(properties == null || properties.size() == 0) {
			properties.load(new FileInputStream(this.fileUtenze));
			if(properties == null || properties.size() == 0) {
				throw new IOException("File " + this.fileUtenze + " non esistente o vuoto");
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
	
	
//	private static ObjectMapper mapper = new ObjectMapper();	
	public class BasicAuthenticationEntryPoint extends org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint {
		
		public BasicAuthenticationEntryPoint() {
			this.setRealmName(REALM_NAME);
		}

//		@Override
//		public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
//			Problem problemModel = new Problem();
//			problemModel.status(HttpStatus.UNAUTHORIZED.value());
//			problemModel.title(HttpStatus.UNAUTHORIZED.getReasonPhrase());
//			problemModel.detail(authException.getMessage());
//			
//			// imposto il content-type della risposta
//			response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
//			
//			ServletOutputStream outputStream = null;
//			try{
//				response.setStatus(problemModel.getStatus());
//				outputStream = response.getOutputStream();
//				mapper.writeValue(outputStream, problemModel);
//				outputStream.flush();
//			}catch(Exception e) {
//
//			} finally {
//			}
//		}
		
		
	}
}
