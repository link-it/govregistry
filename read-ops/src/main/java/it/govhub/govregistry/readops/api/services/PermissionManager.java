package it.govhub.govregistry.readops.api.services;

import java.util.Set;

import it.govhub.govregistry.commons.entity.UserEntity;

/**
 * Interfaccia che le singole applicazioni di GovHub dovranno implementare.
 * 
 * Dice quali oggetti di govhub l'utente è autorizzato a leggere. 
 *
 */
public interface PermissionManager {

	public Set<Long> listReadableOrganizations(UserEntity user);
	
	public Set<Long> listReadableServices(UserEntity user);
	
}
