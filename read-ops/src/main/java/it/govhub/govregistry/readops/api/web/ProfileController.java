package it.govhub.govregistry.readops.api.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import it.govhub.govregistry.commons.api.beans.Profile;
import it.govhub.govregistry.commons.config.ApplicationConfig;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.readops.api.assemblers.ProfileAssembler;
import it.govhub.govregistry.readops.api.spec.ProfileApi;
import it.govhub.security.beans.GovhubPrincipal;


@V1RestController
public class ProfileController implements ProfileApi {
	
	@Autowired
	ProfileAssembler profileAssembler;
	
	@Autowired
	ApplicationConfig appConfig;
	
	@Override
	public ResponseEntity<Profile> profile() {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		GovhubPrincipal principal = (GovhubPrincipal) authentication.getPrincipal();
		
		Profile ret = this.profileAssembler.toModel(principal.getUser(), this.appConfig.getApplicationId());
		return ResponseEntity.ok(ret);
	}

}
