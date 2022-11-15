package it.govhub.rest.backoffice.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.rest.backoffice.entity.RoleAuthorizationEntity;

public interface RoleAuthorizationRepository  extends JpaRepositoryImplementation<RoleAuthorizationEntity, Long> {

}
