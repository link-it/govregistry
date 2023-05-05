/*
 * GovRegistry - Registries manager for GovHub
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govhub.security.services;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.RoleAuthorizationEntity;
import it.govhub.govregistry.commons.entity.RoleEntity;
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
	
	static Logger log = LoggerFactory.getLogger(SecurityService.class);
	
	public static UserEntity getPrincipal() {
		log.debug("Retrieving GovhubPrincipal from the Authentication Context");
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		GovhubPrincipal principal = (GovhubPrincipal) authentication.getPrincipal();
		return  principal.getUser();
	}
	
	public boolean hasAnyRole(String ...roles) {
		UserEntity user = getPrincipal();
		Set<String> roleList = Set.of(roles);
		
		log.debug("Checking if principal {} has any of the following roles: {}", user.getPrincipal(),  roleList);
		
		OffsetDateTime now = OffsetDateTime.now();
		
		log.debug("Authorizations: ");
		for(var auth : user.getAuthorizations()) {
			log.debug(auth.getRole().getName());
		}
		
		// Cerco fra le autorizzazioni una che abbia uno dei ruoli specificati e che non sia scaduta
		return user.getAuthorizations().stream()
			.filter( auth -> auth.getExpirationDate() == null || now.compareTo(auth.getExpirationDate()) < 0 )
			.anyMatch( auth -> roleList.contains(auth.getRole().getName()) );
		
	}


	public void expectAnyRole(String ...roles) {
		if (!hasAnyRole(roles)) {
			throw new NotAuthorizedException();
		}
	}
	
	public boolean isGovregistryAdmin() {
		return hasAnyRole(GovregistryRoles.GOVREGISTRY_SYSADMIN);
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
		log.debug("Checking if principal can write the requested authorization with authority: {}", authToEdit.getRole().getName()); 

		UserEntity principal = getPrincipal();
		
		if (isGovregistryAdmin()) {
			return true;
		}
		
		OffsetDateTime now = OffsetDateTime.now();

		var roles = principal.getAuthorizations().stream()
				.filter( auth -> auth.getExpirationDate() == null || now.compareTo(auth.getExpirationDate()) < 0 )
				.map(RoleAuthorizationEntity::getRole)
				.filter( role -> role.getAssignableRoles().contains(authToEdit.getRole()))
				.map(RoleEntity::getName)
				.collect(Collectors.toSet());
		
		var authorizedOrganizations = this.listAuthorizedOrganizations(roles);
		var authorizedServices = this.listAuthorizedServices(roles);

		
		boolean canWriteOrganizations = true;
		if (authorizedOrganizations != null) {

			if (authToEdit.getOrganizations().isEmpty()) {
				// Se ho restrizioni sulle organizzazioni da autorizzare e sto chiedendo di abilitare per tutte le organizzazioni allora nisba
				canWriteOrganizations = false;
			} else {
				// Se ho restrizioni sulle organizzazioni da autorizzare e sto chiedendo di abilitare per un certo set di organizzazioni allora controllo
				var  notAuthorizedOrgs = authToEdit.getOrganizations().stream().map(OrganizationEntity::getId).collect(Collectors.toSet());
				notAuthorizedOrgs.removeAll(authorizedOrganizations);
				canWriteOrganizations = notAuthorizedOrgs.isEmpty();
			}
		}
		
		// Faccio loi stesso per i servizi
		boolean canWriteServices = true;
		if (authorizedServices != null) {
			if (authToEdit.getServices().isEmpty()) {
				canWriteServices = false; 
			} else {
				var notAuthorizedServices = authToEdit.getServices().stream().map(ServiceEntity::getId).collect(Collectors.toSet());
				notAuthorizedServices.removeAll(authorizedServices);
				canWriteServices = notAuthorizedServices.isEmpty();
			}
		}
		
	    return canWriteOrganizations && canWriteServices;
	}

	
	/**
	 * Dice se il principal ha delle authority valide con i ruoli specificati su un dato servizio 
	*
	*/
	public void hasAnyServiceAuthority(Long serviceId, String ...roles) {
		log.debug("Checking if principal has any of the following authorities on service [{}]: {}", serviceId, (Object[]) roles);
		
		UserEntity user = getPrincipal();
		
		Set<String> roleList = Set.of(roles);
		OffsetDateTime now = OffsetDateTime.now();
		
		boolean authorized = user.getAuthorizations().stream()
		.filter( auth -> auth.getExpirationDate() == null || now.compareTo(auth.getExpirationDate()) < 0 )
		.anyMatch( auth -> {
			
			boolean hasRole = roleList.contains(auth.getRole().getName());
			boolean onService = auth.getServices().isEmpty() ||  auth.getServices().stream().anyMatch( s-> s.getId().equals(serviceId));
			
			return hasRole && onService;
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
		log.debug("Checking if principal has any of the following authorities on organization [{}]: {}", organizationId, (Object[]) roles);
		
		UserEntity user = getPrincipal();
		
		Set<String> roleList = Set.of(roles);
		OffsetDateTime now = OffsetDateTime.now();
		
		boolean authorized = user.getAuthorizations().stream()
		.filter( auth -> auth.getExpirationDate() == null || now.compareTo(auth.getExpirationDate()) < 0 )
		.anyMatch( auth -> {
			
			boolean hasRole = roleList.contains(auth.getRole().getName());
			boolean onOrganization = auth.getOrganizations().isEmpty() ||  auth.getOrganizations().stream().anyMatch( s-> s.getId().equals(organizationId));
			
			return hasRole && onOrganization;
		});
		
		if (!authorized) {
			throw new NotAuthorizedException();
		}
	}
	

	
	public Set<Long> listAuthorizedOrganizations(String... roles) {
		return listAuthorizedOrganizations(Set.of(roles));
	}
	
	
	public Set<Long> listAuthorizedOrganizations(Collection<String>  roles) {
		return listAuthorizedOrganizations(new HashSet<>(roles));
	}
	
	/**
	 * Elenca le organizzazioni sulle quali si hanno dei permessi.
	 * Ogni ruolo ha associate delle organizzazioni, mi dice tutte le organizzazioni alle quali ho accesso secondo quei ruoli. 
	*
	 * @return   - null se non ho restrizioni (può diventare un Optional)
	 *						- Set<Long>  l'insieme di id sul quale sono autorizzato a lavorare 
	 */
	public Set<Long> listAuthorizedOrganizations(Set<String> roles) {
		log.debug("Retrieving organizations for which the principal has the following authorities: {}", roles);
		
		UserEntity user = getPrincipal();
		OffsetDateTime now = OffsetDateTime.now();
		
		List<RoleAuthorizationEntity> validAuths = user.getAuthorizations().stream()
				.filter( auth -> auth.getExpirationDate() == null || now.compareTo(auth.getExpirationDate()) < 0 )
				.filter( auth -> roles.contains(auth.getRole().getName()))
				.collect(Collectors.toList());
		
		Set<Long> orgIds = new HashSet<>();
		
		for (var auth: validAuths) {
			if (auth.getOrganizations().isEmpty()) {
				orgIds = null;
				break;
			} else {
				orgIds.addAll(auth.getOrganizations().stream().map(OrganizationEntity::getId).collect(Collectors.toList()));
			}
		}
		
		return orgIds;
	}
	
	
	public Set<Long> listAuthorizedServices(String... roles) {
		return listAuthorizedServices(Set.of(roles));
	}
	
	
	public Set<Long> listAuthorizedServices(Collection<String> roles) {
		return listAuthorizedServices(new HashSet<>(roles));
	}


	/**
	 * Elenca i servizi sui quali si hanno dei permessi.
	 * Ogni ruolo ha associate dei servizi, mi dice tutti i servizi ai quali ho accesso secondo quei ruoli. 
	*
	 * @return   - null se non ho restrizioni (può diventare un Optional)
	 *						- Set<Long>  l'insieme di id sul quale sono autorizzato a lavorare 
	 */
	public Set<Long> listAuthorizedServices(Set<String>  roles) {
		log.debug("Retrieving services for which the principal has the following authorities: {}", roles);
		
		UserEntity user = getPrincipal();
		OffsetDateTime now = OffsetDateTime.now();

		var validAuths = user.getAuthorizations().stream()
				.filter( auth -> auth.getExpirationDate() == null || now.compareTo(auth.getExpirationDate()) < 0 )
				.filter( auth -> roles.contains(auth.getRole().getName()))
				.collect(Collectors.toList());
		
		Set<Long> serviceIds = new HashSet<>();
		
		for (var auth: validAuths) {
			if (auth.getServices().isEmpty()) {
				serviceIds = null;
				break;
			} else {
				serviceIds.addAll(auth.getServices().stream().map(ServiceEntity::getId).collect(Collectors.toList()));
			}
		}
		
		return serviceIds;
	}


	/**
	 * Ho l'insieme di id auth1 sui cui posso lavorare. Lo restringo sull'insieme auth2.
	 *
	 *		null = nessuna restrizione
	 *	 	[ id1, id2, ... idN ] = autorizzato solo sui seguenti id
	 */
	 public static Set<Long> restrictAuthorizations(Set<Long> auth1, Set<Long> auth2) {
		 if (auth1 == null) {
			 // Se non ho restrizioni in origine, restituiscimi le seconde restrizioni
			 return auth2;
		 }
		 
		 if (auth2 == null) {
			 // Se non sto restringendo, restituisci le prime restrizioni
			 return auth1;
		 }
		 
		 // Altrimenti restringi le prime con le seconde facendo l'intersezione
		 var ret = new HashSet<Long>(auth1);
		 ret.retainAll(auth2);
		 return ret;
		 
	}


}
