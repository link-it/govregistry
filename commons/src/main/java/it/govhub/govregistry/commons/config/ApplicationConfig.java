package it.govhub.govregistry.commons.config;

import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Bean che le singole applicazioni implementano e che descrive la configurazione necessaria alla parte
 * comune delle applicazioni. 
 *
 */
@Component
public interface ApplicationConfig {

	public String getApplicationId();
	
	public Set<String> getReadServiceRoles();
	
	public Set<String> getReadOrganizationRoles();
}
