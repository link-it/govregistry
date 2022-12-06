package it.govhub.govregistry.api.test.utils;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import it.govhub.govregistry.api.config.SecurityConstants;
import it.govhub.govregistry.api.security.GovhubUserDetailService;

@Component
public class UserAuthProfilesUtils {
	
	@Autowired
	private GovhubUserDetailService userDetailService;

	@Transactional
	public RequestPostProcessor utenzaAdmin() {
		return utenzaPrincipal("amministratore");
	}

	public static RequestPostProcessor utenzaUserViewer() {
	    return user("username").password("password").roles(SecurityConstants.RUOLO_GOVHUB_USERS_VIEWER);
	}

	public static RequestPostProcessor utenzaUserEditor() {
	    return user("username").password("password").roles(SecurityConstants.RUOLO_GOVHUB_USERS_EDITOR);
	}

	public static RequestPostProcessor utenzaOspite() {
	    return user("username").password("password").roles(SecurityConstants.RUOLO_GOVHUB_USER);
	}

	public RequestPostProcessor utenzaPrincipal(String principal) {
		return user(this.userDetailService.loadUserByUsername(principal));
	}
}
