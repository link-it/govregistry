package it.govhub.govregistry.api.repository;

import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govregistry.api.config.Caches;
import it.govhub.govregistry.api.entity.RoleAuthorizationEntity_;
import it.govhub.govregistry.api.entity.RoleEntity_;
import it.govhub.govregistry.api.entity.UserEntity;
import it.govhub.govregistry.api.entity.UserEntity_;

public interface UserRepository extends JpaRepositoryImplementation<UserEntity, Long>{
	
	public Optional<UserEntity> findByPrincipal(String principal);

	/**
	 * Utilizziamo l'EntityGraph per risultare in una sola query invece che N quando preleviamo il principal.
	 */
    @EntityGraph(attributePaths = {
    		UserEntity_.AUTHORIZATIONS, 
    		UserEntity_.AUTHORIZATIONS+"."+RoleAuthorizationEntity_.SERVICES, 
    		UserEntity_.AUTHORIZATIONS+"."+RoleAuthorizationEntity_.ORGANIZATIONS,
    		UserEntity_.AUTHORIZATIONS+"."+RoleAuthorizationEntity_.ROLE,
    		UserEntity_.AUTHORIZATIONS+"."+RoleAuthorizationEntity_.ROLE+"."+RoleEntity_.ASSIGNABLE_ROLES
    		})
	public Optional<UserEntity> findAndPreloadByPrincipal(String principal);

	
	@Override
	@CacheEvict(cacheNames=Caches.PRINCIPALS, key = "#entity.principal")
	public <S extends UserEntity> S save(S entity);

}
