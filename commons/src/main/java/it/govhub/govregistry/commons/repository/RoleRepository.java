package it.govhub.govregistry.commons.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govregistry.commons.entity.RoleEntity;

public interface RoleRepository  extends JpaRepositoryImplementation<RoleEntity, Long>{

}
