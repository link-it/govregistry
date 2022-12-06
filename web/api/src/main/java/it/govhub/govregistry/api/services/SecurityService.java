package it.govhub.govregistry.api.services;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import it.govhub.govregistry.api.config.SecurityConstants;
import it.govhub.govregistry.api.entity.UserEntity;
import it.govhub.govregistry.api.exception.NotAuthorizedException;
import it.govhub.govregistry.api.security.GovhubPrincipal;


/**
 * Query e Asserzioni sui permessi degli utenti.
 * Se il numero di query per la verifica di autorizzazioni diventa alto, per ottimizzare è possibile rendere
 * EAGER la collezione di authorizations per la UserEntity e verificare in ram invece che su db
 *
 */
@Service
public class SecurityService {
	
	public static UserEntity getPrincipal() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		GovhubPrincipal principal = (GovhubPrincipal) authentication.getPrincipal();
		return  principal.getUser();
	}


	public void hasAnyRole(String ...roles) {
		UserEntity user = getPrincipal();
		
		Set<String> roleList = Set.of(roles);
		OffsetDateTime now = OffsetDateTime.now();
		
		// Cerco fra le autorizzazioni una che abbia uno dei ruoli specificati e che non sia scaduta
		user.getAuthorizations().stream()
			.filter( auth -> auth.getExpirationDate() == null || now.compareTo(auth.getExpirationDate()) < 0 )
			.filter( auth -> roleList.contains(auth.getRole().getName()))
			.findAny()
			.orElseThrow( () -> new NotAuthorizedException());
	}

	
	/**
	 * Dice se il principal ha delle authority valide con i ruoli specificati su un dato servizio 
	*
	*/
	public void hasAnyServiceAuthority(Long serviceId, String ...roles) {
		UserEntity user = getPrincipal();
		
		Set<String> roleList = Set.of(roles);
		OffsetDateTime now = OffsetDateTime.now();
		
		user.getAuthorizations().stream()
		.filter( auth -> auth.getExpirationDate() == null || now.compareTo(auth.getExpirationDate()) < 0 )
		.filter( auth -> {
			
			// L'admin non ha bisogno di avere servizi specificati
			boolean isAdmin = auth.getRole().getName().equals(SecurityConstants.RUOLO_GOVHUB_SYSADMIN);
			boolean hasRole = roleList.contains(auth.getRole().getName());
			boolean onService = auth.getServices().isEmpty() ||  auth.getServices().stream().anyMatch( s-> s.getId().equals(serviceId));
			
			return isAdmin || (hasRole && onService);
		})
		.findAny()
		.orElseThrow( () -> new NotAuthorizedException());
	}
	
	
	/**
	 *  Dice se il principal ha delle authority valide con i ruoli specificati su una data organizzazione
	 *  
	 */
	public void hasAnyOrganizationAuthority(Long organizationId, String ...roles) {
		UserEntity user = getPrincipal();
		
		Set<String> roleList = Set.of(roles);
		OffsetDateTime now = OffsetDateTime.now();
		
		user.getAuthorizations().stream()
		.filter( auth -> auth.getExpirationDate() == null || now.compareTo(auth.getExpirationDate()) < 0 )
		.filter( auth -> {
			
			// L'admin non ha bisogno di avere servizi specificati
			boolean isAdmin = auth.getRole().getName().equals(SecurityConstants.RUOLO_GOVHUB_SYSADMIN);
			boolean hasRole = roleList.contains(auth.getRole().getName());
			boolean onOrganization = auth.getOrganizations().isEmpty() ||  auth.getOrganizations().stream().anyMatch( s-> s.getId().equals(organizationId));
			
			return isAdmin || (hasRole && onOrganization);
		})
		.findAny()
		.orElseThrow( () -> new NotAuthorizedException());
	}
	

	/**
	 * Quando si ha una autorizzazione sulle organizzazioni con una lista di organizzazioni vuota, allora il permesso 
	 * è esteso a tutte le organizzazioni e il metodo restituisce true.
	 * 
	 * L'admin può leggere sempre tutto.
	 */
	public boolean canReadAllOrganizations() {
		
		UserEntity user = getPrincipal();
		OffsetDateTime now = OffsetDateTime.now();

		return user.getAuthorizations().stream()
			.filter( auth -> auth.getExpirationDate() == null || now.compareTo(auth.getExpirationDate()) < 0 )
			.filter( auth -> {
				String role = auth.getRole().getName(); 
				boolean isAdmin = role.equals(SecurityConstants.RUOLO_GOVHUB_SYSADMIN);
				boolean hasRole = role.equals(SecurityConstants.RUOLO_GOVHUB_ORGANIZATIONS_VIEWER) || role.equals(SecurityConstants.RUOLO_GOVHUB_ORGANIZATIONS_EDITOR);
				return isAdmin || (hasRole && auth.getOrganizations().isEmpty());
			})
			.findAny()
			.isPresent();
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
		OffsetDateTime now = OffsetDateTime.now();

		return user.getAuthorizations().stream()
			.filter( auth -> auth.getExpirationDate() == null || now.compareTo(auth.getExpirationDate()) < 0 )
			.filter( auth -> {
				String role = auth.getRole().getName(); 
				boolean isAdmin = role.equals(SecurityConstants.RUOLO_GOVHUB_SYSADMIN);
				boolean hasRole = role.equals(SecurityConstants.RUOLO_GOVHUB_SERVICES_VIEWER) || role.equals(SecurityConstants.RUOLO_GOVHUB_SERVICES_EDITOR);
				return isAdmin || (hasRole && auth.getServices().isEmpty());
			})
			.findAny()
			.isPresent();
		
	}
	
	

	/**
	 * Elenca le organizzazioni sulle quali si hanno dei permessi. 
	*
	 * @return gli Id delle organizzazioni autorizzate sui ruoli.
	 * 
	 */
	public Set<Long> listAuthorizedOrganizations(String... roles) {
		UserEntity user = getPrincipal();
		OffsetDateTime now = OffsetDateTime.now();
		
		Set<String> roleList = Set.of(roles);
		
		return user.getAuthorizations().stream()
			.filter( auth -> auth.getExpirationDate() == null || now.compareTo(auth.getExpirationDate()) < 0 )
			.filter( auth -> roleList.contains(auth.getRole().getName()))
			.flatMap( auth -> auth.getOrganizations().stream() )
			.map( org -> org.getId())
			.collect(Collectors.toSet());
			
	}
	
	
	/**
	 * Elenca I servizi sui quali si hanno dei permessi. 
	*
	 * @return gli Id dei servizi autorizzati  sui ruoli.
	 */
	
	@Transactional
	public Set<Long> listAuthorizedServices(String... roles) {
		UserEntity user = getPrincipal();
		OffsetDateTime now = OffsetDateTime.now();
		
		Set<String> roleList = Set.of(roles);
		
		return user.getAuthorizations().stream()
			.filter( auth -> auth.getExpirationDate() == null || now.compareTo(auth.getExpirationDate()) < 0 )
			.filter( auth -> roleList.contains(auth.getRole().getName()))
			.flatMap( auth -> auth.getServices().stream() )
			.map( org -> org.getId())
			.collect(Collectors.toSet());
	}


}
