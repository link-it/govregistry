package it.govhub.govregistry.readops.api.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govregistry.commons.entity.RoleEntity;

public interface RoleRepository  extends JpaRepositoryImplementation<RoleEntity, Long>{

}
