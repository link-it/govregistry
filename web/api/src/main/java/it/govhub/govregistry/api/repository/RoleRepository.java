package it.govhub.govregistry.api.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govregistry.api.entity.RoleEntity;

public interface RoleRepository  extends JpaRepositoryImplementation<RoleEntity, Long>{

}
