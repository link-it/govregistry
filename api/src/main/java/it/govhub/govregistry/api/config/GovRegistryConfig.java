package it.govhub.govregistry.api.config;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.config.ApplicationConfig;
import it.govhub.security.config.GovregistryRoles;

@Component
public class GovRegistryConfig implements ApplicationConfig {
	
	@Value("${application-id:govregistry}")
	String applicationId;

	private static final Set<String> DEFAULT_READ_SERVICE_ROLES = Set.of(
			GovregistryRoles.GOVREGISTRY_SYSADMIN ,
			GovregistryRoles.GOVREGISTRY_SERVICES_EDITOR,
			GovregistryRoles.GOVREGISTRY_SERVICES_VIEWER);

	
	private static final Set<String> DEFAULT_READ_ORGANIZATION_ROLES = Set.of(
			GovregistryRoles.GOVREGISTRY_ORGANIZATIONS_EDITOR,
			GovregistryRoles.GOVREGISTRY_ORGANIZATIONS_VIEWER,
			GovregistryRoles.GOVREGISTRY_SYSADMIN);
	
	@Override
	public String getApplicationId() {
		return this.applicationId;
	}
	
	@Override
	public Set<String> getReadServiceRoles() {
		return DEFAULT_READ_SERVICE_ROLES;
	}

	@Override
	public Set<String> getReadOrganizationRoles() {
		return DEFAULT_READ_ORGANIZATION_ROLES;
	}

}
