package it.govhub.govregistry.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.config.ApplicationConfig;

@Component
public class GovRegistryConfig implements ApplicationConfig {
	
	@Value("${application-id:govregistry")
	private String applicationId;

	@Override
	public String getApplicationId() {
		return this.applicationId;
	}

}
