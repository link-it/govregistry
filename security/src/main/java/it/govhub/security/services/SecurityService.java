package it.govhub.security.services;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.RoleAuthorizationEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.exception.NotAuthorizedException;
import it.govhub.security.beans.GovhubPrincipal;
import it.govhub.security.config.GovregistryRoles;


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
	
	Logger logger = LoggerFactory.getLogger(SecurityService.class);
	
	
	public boolean hasAnyRole(String ...roles) {
		
		UserEntity user = getPrincipal();
		
		Set<String> roleList = Set.of(roles);
		OffsetDateTime now = OffsetDateTime.now();
		
		// Cerco fra le autorizzazioni una che abbia uno dei ruoli specificati e che non sia scaduta
		return user.getAuthorizations().stream()
			.filter( auth -> auth.getExpirationDate() == null || now.compareTo(auth.getExpirationDate()) < 0 )
			.anyMatch( auth -> {
						logger.debug("Checking role: {} in roles: {}", auth.getRole().getName(), roleList );
						return roleList.contains(auth.getRole().getName());
					});
		
	}


	public void expectAnyRole(String ...roles) {
		if (!hasAnyRole(roles)) {
			throw new NotAuthorizedException();
		}
	}
	
	public boolean isAdmin() {
		return hasAnyRole(GovregistryRoles.RUOLO_GOVHUB_SYSADMIN);
	}
	
	
	/**
	 * Restituisce true se il principal può assegnare o rimuovere l'autorizzazione argomento.
	 *
	 *  In quanto principal posso assegnare solo i ruoli che sono fra i mei assignable_roles.
	 *  Posso assegnarli se il principal ha una autorizzazione con un ruolo che ha fra gli assignable_roles il ruolo da assegnare
	 *  e la cui expiration_date è superiore a quella del ruolo da assegnare.
	 *  Devo cercare le RoleAuthorizationEntity che hanno questa caratteristica.
	 *  Da queste entità trovate vado a controllare se posso lavorare su tutte le organizzazioni e servizi specificati dalla autorizzazione da creare.
	 * 
	 */
	public boolean canWriteAuthorization(RoleAuthorizationEntity authToEdit) {
		if (isAdmin()) {
			return true;
		}
		
		OffsetDateTime now = OffsetDateTime.now();

		UserEntity principal = getPrincipal();
		
		Iterable<RoleAuthorizationEntity> validAuths = principal.getAuthorizations().stream()
				.filter( auth -> auth.getExpirationDate() == null || now.compareTo(auth.getExpirationDate()) < 0 )
				.filter( auth -> auth.getRole().getAssignableRoles().contains(authToEdit.getRole()))
				::iterator;
				
		// Assumo di non essere autorizzato per tutte le organizzazioni e servizi e rimuovo mano mano 
		// quelli che incontro, devo anche avere almeno una validAuths
		Set<OrganizationEntity> notAuthorizedOrgs = new HashSet<>(authToEdit.getOrganizations());
		Set<ServiceEntity> notAuthorizedServices = new HashSet<>(authToEdit.getServices());
		boolean hasValidAuths = false;
		
	    for (var auth : validAuths) {
	    	hasValidAuths = true;
	    	
			if (notAuthorizedOrgs.isEmpty() && notAuthorizedServices.isEmpty() )  {
				break;
			}
			
			if (auth.getOrganizations().isEmpty()) {
				notAuthorizedOrgs.clear();
			} else {
				notAuthorizedOrgs.removeAll(auth.getOrganizations());
			}
			
			if (auth.getServices().isEmpty()) {
				notAuthorizedServices.clear();
			} else {
				notAuthorizedServices.removeAll(auth.getServices());
			}
	    }
	    
	    return hasValidAuths && notAuthorizedOrgs.isEmpty() && notAuthorizedServices.isEmpty();
	}

	
	/**
	 * Dice se il principal ha delle authority valide con i ruoli specificati su un dato servizio 
	*
	*/
	public void hasAnyServiceAuthority(Long serviceId, String ...roles) {
		UserEntity user = getPrincipal();
		
		Set<String> roleList = Set.of(roles);
		OffsetDateTime now = OffsetDateTime.now();
		
		boolean authorized = user.getAuthorizations().stream()
		.filter( auth -> auth.getExpirationDate() == null || now.compareTo(auth.getExpirationDate()) < 0 )
		.anyMatch( auth -> {
			
			// L'admin non ha bisogno di avere servizi specificati
			boolean isAdmin = auth.getRole().getName().equals(GovregistryRoles.RUOLO_GOVHUB_SYSADMIN);
			boolean hasRole = roleList.contains(auth.getRole().getName());
			boolean onService = auth.getServices().isEmpty() ||  auth.getServices().stream().anyMatch( s-> s.getId().equals(serviceId));
			
			return isAdmin || (hasRole && onService);
		});
		
		if (!authorized) {
			throw new NotAuthorizedException();
		}
	}
	
	
	/**
	 *  Dice se il principal ha delle authority valide con i ruoli specificati su una data organizzazione
	 *  
	 */
	public void hasAnyOrganizationAuthority(Long organizationId, String ...roles) {
		UserEntity user = getPrincipal();
		
		Set<String> roleList = Set.of(roles);
		OffsetDateTime now = OffsetDateTime.now();
		
		boolean authorized = user.getAuthorizations().stream()
		.filter( auth -> auth.getExpirationDate() == null || now.compareTo(auth.getExpirationDate()) < 0 )
		.anyMatch( auth -> {
			
			// L'admin non ha bisogno di avere servizi specificati
			boolean isAdmin = auth.getRole().getName().equals(GovregistryRoles.RUOLO_GOVHUB_SYSADMIN);
			boolean hasRole = roleList.contains(auth.getRole().getName());
			boolean onOrganization = auth.getOrganizations().isEmpty() ||  auth.getOrganizations().stream().anyMatch( s-> s.getId().equals(organizationId));
			
			return isAdmin || (hasRole && onOrganization);
		});
		
		if (!authorized) {
			throw new NotAuthorizedException();
		}
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
			.anyMatch( auth -> {
				String role = auth.getRole().getName(); 
				boolean isAdmin = role.equals(GovregistryRoles.RUOLO_GOVHUB_SYSADMIN);
				boolean hasRole = role.equals(GovregistryRoles.RUOLO_GOVREGISTRY_ORGANIZATIONS_VIEWER) || role.equals(GovregistryRoles.RUOLO_GOVREGISTRY_ORGANIZATIONS_EDITOR);
				return isAdmin || (hasRole && auth.getOrganizations().isEmpty());
			});
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
			.anyMatch( auth -> {
				String role = auth.getRole().getName(); 
				boolean isAdmin = role.equals(GovregistryRoles.RUOLO_GOVHUB_SYSADMIN);
				boolean hasRole = role.equals(GovregistryRoles.RUOLO_GOVREGISTRY_SERVICES_VIEWER) || role.equals(GovregistryRoles.RUOLO_GOVREGISTRY_SERVICES_EDITOR);
				return isAdmin || (hasRole && auth.getServices().isEmpty());
			});
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
			.map( OrganizationEntity::getId)	
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
			.map( ServiceEntity::getId )
			.collect(Collectors.toSet());
	}


}
