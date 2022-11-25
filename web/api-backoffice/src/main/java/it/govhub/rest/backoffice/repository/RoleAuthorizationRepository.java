package it.govhub.rest.backoffice.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.rest.backoffice.config.Caches;
import it.govhub.rest.backoffice.entity.RoleAuthorizationEntity;

public interface RoleAuthorizationRepository  extends JpaRepositoryImplementation<RoleAuthorizationEntity, Long> {
	
	@Override
	@CacheEvict(cacheNames=Caches.PRINCIPALS, key = "#entity.user.principal")
	public <S extends RoleAuthorizationEntity> S save(S entity);

}
