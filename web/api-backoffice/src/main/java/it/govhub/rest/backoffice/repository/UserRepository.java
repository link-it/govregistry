package it.govhub.rest.backoffice.repository;

import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.rest.backoffice.config.Caches;
import it.govhub.rest.backoffice.entity.UserEntity;

public interface UserRepository extends JpaRepositoryImplementation<UserEntity, Long>{
	
	public Optional<UserEntity> findByPrincipal(String principal);
	
	@Override
	@CacheEvict(cacheNames=Caches.PRINCIPALS, key = "#entity.principal")
	public <S extends UserEntity> S save(S entity);

}
