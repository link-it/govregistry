package it.govhub.rest.backoffice.test.utils;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import it.govhub.rest.backoffice.config.SecurityConfig;


public class UserAuthProfilesUtils {

	public static RequestPostProcessor utenzaAdmin() {
	    return user("username").password("password").roles(SecurityConfig.RUOLO_GOVHUB_SYSADMIN);
	}

	public static RequestPostProcessor utenzaUserViewer() {
	    return user("username").password("password").roles(SecurityConfig.RUOLO_GOVHUB_USERS_VIEWER);
	}

	public static RequestPostProcessor utenzaUserEditor() {
	    return user("username").password("password").roles(SecurityConfig.RUOLO_GOVHUB_USERS_EDITOR);
	}

	public static RequestPostProcessor utenzaOspite() {
	    return user("username").password("password").roles(SecurityConfig.RUOLO_GOVHUB_USER);
	}


}
