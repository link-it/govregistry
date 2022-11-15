package it.govhub.rest.backoffice.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.rest.backoffice.entity.RoleEntity;

public interface RoleRepository  extends JpaRepositoryImplementation<RoleEntity, Long>{

}
