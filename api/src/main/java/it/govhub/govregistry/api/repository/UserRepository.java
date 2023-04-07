/*
 * GovRegistry - Registries manager for GovHub
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govhub.govregistry.api.repository;

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
