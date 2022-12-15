package it.govhub.govregistry.commons.repository;

import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govregistry.commons.cache.Caches;
import it.govhub.govregistry.commons.entity.RoleAuthorizationEntity_;
import it.govhub.govregistry.commons.entity.RoleEntity_;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.entity.UserEntity_;

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

	/**
	 * NOTA: Dopo aver semplificato i pom e tolto il parent di spring boot, non è stato più possibile
	 * utilizzare il nome del parametro nell'espressione SpEL.
	 * E' necessario utilizzare la notazione posizionale #p0.
	 * Probabilmente l'inclusione di spring boot come parent porta alla scrittura di alcune informazioni di debug
	 * nel jar.  
	 */
	@Override
	@CacheEvict(cacheNames=Caches.PRINCIPALS, key = "#p0.principal")
	public <S extends UserEntity> S save(S entity);

}
