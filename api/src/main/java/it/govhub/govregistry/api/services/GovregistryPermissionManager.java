package it.govhub.govregistry.api.services;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.services.PermissionManager;
import it.govhub.security.config.GovregistryRoles;
import it.govhub.security.services.SecurityService;

@Service
public class GovregistryPermissionManager implements PermissionManager {

	@Autowired
	SecurityService authService;
	
	@Override
	public Set<Long> listReadableOrganizations(UserEntity user) {
		return this.authService.listAuthorizedOrganizations(
				GovregistryRoles.GOVREGISTRY_ORGANIZATIONS_EDITOR,
				GovregistryRoles.GOVREGISTRY_ORGANIZATIONS_VIEWER,
				GovregistryRoles.GOVREGISTRY_SYSADMIN);
	}

	
	@Override
	public Set<Long> listReadableServices(UserEntity user) {
		return this.authService.listAuthorizedServices(
				GovregistryRoles.GOVREGISTRY_SERVICES_EDITOR, 
				GovregistryRoles.GOVREGISTRY_SERVICES_VIEWER,
				GovregistryRoles.GOVREGISTRY_SYSADMIN);
	}

}
