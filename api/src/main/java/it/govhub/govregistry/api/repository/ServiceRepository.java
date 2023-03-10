package it.govhub.govregistry.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govregistry.commons.entity.ServiceEntity;

public interface ServiceRepository  extends JpaRepositoryImplementation<ServiceEntity, Long>{
	
	public Optional<ServiceEntity> findByName(String name);
	
}
