package it.govhub.security.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govregistry.commons.entity.RoleAuthorizationEntity_;
import it.govhub.govregistry.commons.entity.RoleEntity_;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.entity.UserEntity_;


public interface SecurityUserRepository extends JpaRepositoryImplementation<UserEntity, Long>{
	
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
}