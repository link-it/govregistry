package it.govhub.security.caches;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import it.govhub.govregistry.commons.cache.Caches;

@Configuration
public class PrincipalCacheTTLEvicter {
	
	private Logger logger = LoggerFactory.getLogger(PrincipalCacheTTLEvicter.class);

	@CacheEvict(value = Caches.PRINCIPALS, allEntries = true)
	@Scheduled(fixedRateString = "${caching.govhub.principals.TTL}")
	public void emptyPrincipalsCache() {
		logger.info("CLEARING " + Caches.PRINCIPALS + " CACHE.");
	}
	
}
	
