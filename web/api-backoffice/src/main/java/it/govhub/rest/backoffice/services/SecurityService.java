package it.govhub.rest.backoffice.services;

import static it.govhub.rest.backoffice.config.SecurityConfig.RUOLO_GOVHUB_ORGANIZATIONS_EDITOR;
import static it.govhub.rest.backoffice.config.SecurityConfig.RUOLO_GOVHUB_ORGANIZATIONS_VIEWER;
import static it.govhub.rest.backoffice.config.SecurityConfig.RUOLO_GOVHUB_SERVICES_EDITOR;
import static it.govhub.rest.backoffice.config.SecurityConfig.RUOLO_GOVHUB_SERVICES_VIEWER;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import it.govhub.rest.backoffice.config.SecurityConfig;
import it.govhub.rest.backoffice.entity.OrganizationEntity;
import it.govhub.rest.backoffice.entity.RoleAuthorizationEntity;
import it.govhub.rest.backoffice.entity.ServiceEntity;
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
	private RoleAuthorizationRepository authRepo;


	// Oltre a controllare il ruolo viene controllata l'expiration date
	public void hasAnyRole(String ...roles) {
		
		UserEntity user = getPrincipal();
		
		
		Specification<RoleAuthorizationEntity> spec = RoleAuthorizationFilters.byRoleName(roles)
				.and(RoleAuthorizationFilters.byUser(user.getId()))
				.and(RoleAuthorizationFilters.expiresAfter(OffsetDateTime.now()));
		
		List<RoleAuthorizationEntity> authorizations = this.authRepo.findAll(spec);
		
		if (authorizations.isEmpty()) {
			throw new NotAuthorizedException();
		}
	}

	

	/**
	 * Dice se il principal ha delle authority valide con i ruoli specificati su un dato servizio 
	*
	*/
	public void hasAnyServiceAuthority(Long serviceId, String ...roles) {
		
		UserEntity user = getPrincipal();
		
		// L'admin lavora automaticamente su tutte le organizzazioni e servizi, per cui non serve controllare
		// puntualmente il servizio
		
		Specification<RoleAuthorizationEntity> adminSpec =  RoleAuthorizationFilters.byRoleName(SecurityConfig.RUOLO_GOVHUB_SYSADMIN)
				.and(RoleAuthorizationFilters.byUser(user.getId()))
				.and(RoleAuthorizationFilters.expiresAfter(OffsetDateTime.now()));
		
		Specification<RoleAuthorizationEntity> authoritySpec = RoleAuthorizationFilters.byRoleName(roles)
				.and(RoleAuthorizationFilters.byUser(user.getId()))
				.and(RoleAuthorizationFilters.expiresAfter(OffsetDateTime.now())
				.and(RoleAuthorizationFilters.onService(serviceId)));
		
		List<RoleAuthorizationEntity> authorizations = this.authRepo.findAll(adminSpec.or(authoritySpec));
		
		if (authorizations.isEmpty()) {
			throw new NotAuthorizedException();
		}
	}
	
	
	/**
	 *  Dice se il principal ha delle authority valide con i ruoli specificati su una data organizzazione
	 *  
	 */
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
		
		List<RoleAuthorizationEntity> authorizations = this.authRepo.findAll(adminSpec.or(authoritySpec));
		
		if (authorizations.isEmpty()) {
			throw new NotAuthorizedException();
		}
	}
	
	
	public static UserEntity getPrincipal() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		GovhubPrincipal principal = (GovhubPrincipal) authentication.getPrincipal();
		return  principal.getUser();
	}



	/**
	 * Quando si ha una autorizzazione con una lista di organizzazioni vuota, allora il permesso è esteso a tutte le organizzazioni e il metodo
	 * restituisce true.
	 * 
	 * L'admin può leggere sempre tutto.
	 */
	public boolean canReadAllOrganizations() {
		UserEntity user = getPrincipal();
		
		Specification<RoleAuthorizationEntity> spec = RoleAuthorizationFilters.byRoleName(RUOLO_GOVHUB_ORGANIZATIONS_EDITOR, RUOLO_GOVHUB_ORGANIZATIONS_VIEWER)
				.and(RoleAuthorizationFilters.expiresAfter(OffsetDateTime.now()))
				.and(RoleAuthorizationFilters.byUser(user.getId()))
				.and(RoleAuthorizationFilters.onOrganizations(new ArrayList<Long>()));
		
		return this.authRepo.exists(spec.or(RoleAuthorizationFilters.byAdmin(user.getId())));
	}
	

	/**
	 * Quando si ha una autorizzazione con una lista di servizi vuota, allora il permesso è esteso a tutti i servizi e il metodo
	 * restituisce true.
	 * 
	 * L'admin può leggere sempre tutto.
	 * 
	 */	
	public boolean canReadAllServices() {
		UserEntity user = getPrincipal();
		
		Specification<RoleAuthorizationEntity> spec = RoleAuthorizationFilters.byRoleName(RUOLO_GOVHUB_SERVICES_EDITOR, RUOLO_GOVHUB_SERVICES_VIEWER)
				.and(RoleAuthorizationFilters.expiresAfter(OffsetDateTime.now()))
				.and(RoleAuthorizationFilters.byUser(user.getId()))
				.and(RoleAuthorizationFilters.onServices(Collections.emptyList()));
		
		return this.authRepo.exists(spec.or(RoleAuthorizationFilters.byAdmin(user.getId())));
	}



	/**
	 * Elenca le organizzazioni sulle quali si hanno dei permessi. 
	*
	 * @return gli Id delle organizzazioni autorizzate sui ruoli.
	 */
	
	@Transactional
	public Set<Long> listAuthorizedOrganizations(String... roles) {
		UserEntity user = getPrincipal();
		
		Specification<RoleAuthorizationEntity> spec = RoleAuthorizationFilters.byRoleName(roles)
				.and(RoleAuthorizationFilters.expiresAfter(OffsetDateTime.now()))
				.and(RoleAuthorizationFilters.byUser(user.getId()));
		
		List<RoleAuthorizationEntity> authorizations = this.authRepo.findAll(spec);
		
		Set<Long> organizationIds = authorizations.stream()
			.flatMap( auth -> auth.getOrganizations().stream())
			.map(OrganizationEntity::getId)
			.collect(Collectors.toSet());
		
		return organizationIds;
	}


	/**
	 * Elenca I servizi sui quali si hanno dei permessi. 
	*
	 * @return gli Id dei servizi autorizzati  sui ruoli.
	 */
	
	@Transactional
	public Set<Long> listAuthorizedServices(String... roles) {
		UserEntity user = getPrincipal();
		
		Specification<RoleAuthorizationEntity> spec = RoleAuthorizationFilters.byRoleName(roles)
				.and(RoleAuthorizationFilters.expiresAfter(OffsetDateTime.now()))
				.and(RoleAuthorizationFilters.byUser(user.getId()));
		
		List<RoleAuthorizationEntity> authorizations = this.authRepo.findAll(spec);
		
		Set<Long> serviceIds = authorizations.stream()
			.flatMap( auth -> auth.getServices().stream())
			.map(ServiceEntity::getId)
			.collect(Collectors.toSet());
		
		return serviceIds;
	}

}
