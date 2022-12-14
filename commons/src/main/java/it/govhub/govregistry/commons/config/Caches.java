package it.govhub.govregistry.commons.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class Caches {

	public static final String PRINCIPALS = "principals";
	
	private Logger logger = LoggerFactory.getLogger(Caches.class);
	
	@CacheEvict(value = Caches.PRINCIPALS, allEntries = true)
	@Scheduled(fixedRateString = "${caching.govhub.principals.TTL}")
	public void emptyPrincipalsCache() {
		logger.info("CLEARING " + Caches.PRINCIPALS + " CACHE.");
	}
	
	
}