package it.govhub.govregistry.api.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.config.ApplicationConfig;
import it.govhub.security.config.GovregistryRoles;

@Component
public class GovRegistryConfig implements ApplicationConfig {
	
	@Value("${application-id:govregistry")
	String applicationId;

	@Value("#{systemProperties['govhub.auth.read-service-roles'] ?: T(it.govhub.govregistry.api.config.GovRegistryConfig).DEFAULT_READ_SERVICE_ROLES}")
	Set<String> readServiceRoles;
	
	@Value("#{systemProperties['govhub.auth.read-organization-roles'] ?: T(it.govhub.govregistry.api.config.GovRegistryConfig).DEFAULT_READ_ORGANIZATION_ROLES}")
	Set<String> readOrganizationRoles;
	
	public static Set<String> DEFAULT_READ_SERVICE_ROLES = Set.of(
			GovregistryRoles.GOVREGISTRY_SYSADMIN ,
			GovregistryRoles.GOVREGISTRY_SERVICES_EDITOR,
			GovregistryRoles.GOVREGISTRY_SERVICES_VIEWER);

	
	public static Set<String> DEFAULT_READ_ORGANIZATION_ROLES = Set.of(
			GovregistryRoles.GOVREGISTRY_ORGANIZATIONS_EDITOR,
			GovregistryRoles.GOVREGISTRY_ORGANIZATIONS_VIEWER,
			GovregistryRoles.GOVREGISTRY_SYSADMIN);
	
	@Override
	public String getApplicationId() {
		return this.applicationId;
	}
	
	@Override
	public Set<String> getReadServiceRoles() {
		return new HashSet<>(readServiceRoles);
	}

	@Override
	public Set<String> getReadOrganizationRoles() {
		return new HashSet<>(readOrganizationRoles);
	}

}
