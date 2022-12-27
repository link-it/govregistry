package it.govhub.govregistry.readops.api.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govregistry.commons.entity.RoleAuthorizationEntity;
import it.govhub.security.cache.Caches;

public interface RoleAuthorizationRepository  extends JpaRepositoryImplementation<RoleAuthorizationEntity, Long> {
	
	@Override
	@CacheEvict(cacheNames=Caches.PRINCIPALS, key = "#p0.user.principal")
	public <S extends RoleAuthorizationEntity> S save(S entity);

}
