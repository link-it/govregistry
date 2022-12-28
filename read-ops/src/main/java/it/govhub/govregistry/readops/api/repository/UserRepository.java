package it.govhub.govregistry.readops.api.repository;

import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.security.cache.Caches;

public interface UserRepository extends JpaRepositoryImplementation<UserEntity, Long>{
	
	public Optional<UserEntity> findByPrincipal(String principal);


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
