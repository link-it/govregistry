package it.govhub.govregistry.readops.api.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import it.govhub.govregistry.commons.api.beans.Profile;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.readops.api.assemblers.ProfileAssembler;
import it.govhub.govregistry.readops.api.spec.ProfileApi;
import it.govhub.security.beans.GovhubPrincipal;


@V1RestController
public class ProfileController implements ProfileApi {
	
	@Autowired
	ProfileAssembler profileAssembler;
	
	String applicationId = "govregistry"; // TODO: Cambialo

	@Override
	public ResponseEntity<Profile> profile() {
		
		// TODO: Nella  GET profile dei servizi faccio vedere solo le autorizzazioni che riguardano un certo servizio, anche per govregistry?
		//		Nella GET /authorizazions 
		//			-- Su govregistry faccio vedere tutte le autorizzazioni
		//			-- Su govio ecc.. faccio vedere solo le autorizzazioni dell'applicazione
		//		Idem per le autorizzazioni della /profile
	
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		GovhubPrincipal principal = (GovhubPrincipal) authentication.getPrincipal();
		
		Profile ret = this.profileAssembler.toModel(principal.getUser(), applicationId);
		
		
		return ResponseEntity.ok(ret);
	}

}
