package it.govhub.rest.backoffice.services;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import it.govhub.rest.backoffice.config.SecurityConfig;
import it.govhub.rest.backoffice.entity.RoleAuthorizationEntity;
import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.exception.NotAuthorizedException;
import it.govhub.rest.backoffice.repository.RoleAuthorizationFilters;
import it.govhub.rest.backoffice.repository.RoleAuthorizationRepository;
import it.govhub.rest.backoffice.security.GovhubPrincipal;


/**
 * Query e Asserzioni sui permessi degli utenti.
 * Se il numero di query per la verifica di autorizzazioni diventa alto, per ottimizzare è possibile rendere
 * EAGER la collezione di authorizations per la UserEntity e verificare in ram invece che su db
 *
 */
@Service
public class SecurityService {
	
	@Autowired
	private RoleAuthorizationRepository roleRepo;


	// Oltre a controllare il ruolo viene controllata l'expiration date
	public void hasAnyRole(String ...roles) {
		
		UserEntity user = getPrincipal();
		
		
		Specification<RoleAuthorizationEntity> spec = RoleAuthorizationFilters.byRoleName(roles)
				.and(RoleAuthorizationFilters.byUser(user.getId()))
				.and(RoleAuthorizationFilters.expiresAfter(OffsetDateTime.now()));
		
		List<RoleAuthorizationEntity> authorizations = this.roleRepo.findAll(spec);
		
		if (authorizations.isEmpty()) {
			throw new NotAuthorizedException();
		}
	}

	
	
	public void hasAnyServiceAuthority(Long id, String ...roles) {
		
		UserEntity user = getPrincipal();
		
		// L'admin lavora automaticamente su tutte le organizzazioni e servizi, per cui non serve controllare
		// puntualmente il servizio
		
		Specification<RoleAuthorizationEntity> adminSpec =  RoleAuthorizationFilters.byRoleName(SecurityConfig.RUOLO_GOVHUB_SYSADMIN)
				.and(RoleAuthorizationFilters.byUser(user.getId()))
				.and(RoleAuthorizationFilters.expiresAfter(OffsetDateTime.now()));
		
		Specification<RoleAuthorizationEntity> authoritySpec = RoleAuthorizationFilters.byRoleName(roles)
				.and(RoleAuthorizationFilters.byUser(user.getId()))
				.and(RoleAuthorizationFilters.expiresAfter(OffsetDateTime.now())
				.and(RoleAuthorizationFilters.onService(id)));
		
		List<RoleAuthorizationEntity> authorizations = this.roleRepo.findAll(adminSpec.or(authoritySpec));
		
		if (authorizations.isEmpty()) {
			throw new NotAuthorizedException();
		}
	}
	
	
	public void hasAnyOrganizationAuthority(Long id, String ...roles) {
	
		UserEntity user = getPrincipal();
		
		// L'admin lavora automaticamente su tutte le organizzazioni e servizi, per cui non serve controllare
		// puntualmente l'organizzazione
		
		Specification<RoleAuthorizationEntity> adminSpec =  RoleAuthorizationFilters.byRoleName(SecurityConfig.RUOLO_GOVHUB_SYSADMIN)
				.and(RoleAuthorizationFilters.byUser(user.getId()))
				.and(RoleAuthorizationFilters.expiresAfter(OffsetDateTime.now()));
		
		Specification<RoleAuthorizationEntity> authoritySpec = RoleAuthorizationFilters.byRoleName(roles)
				.and(RoleAuthorizationFilters.byUser(user.getId()))
				.and(RoleAuthorizationFilters.expiresAfter(OffsetDateTime.now())
				.and(RoleAuthorizationFilters.onOrganization(id)));
		
		List<RoleAuthorizationEntity> authorizations = this.roleRepo.findAll(adminSpec.or(authoritySpec));
		
		if (authorizations.isEmpty()) {
			throw new NotAuthorizedException();
		}
	}
	
	
	public static UserEntity getPrincipal() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		GovhubPrincipal principal = (GovhubPrincipal) authentication.getPrincipal();
		return  principal.getUser();
	}

}
