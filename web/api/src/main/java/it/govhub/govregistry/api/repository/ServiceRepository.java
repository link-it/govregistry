package it.govhub.govregistry.api.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govregistry.api.entity.ServiceEntity;

public interface ServiceRepository  extends JpaRepositoryImplementation<ServiceEntity, Long>{}
