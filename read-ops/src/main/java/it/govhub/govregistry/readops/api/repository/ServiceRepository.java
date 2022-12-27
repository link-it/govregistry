package it.govhub.govregistry.readops.api.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govregistry.commons.entity.ServiceEntity;

public interface ServiceRepository  extends JpaRepositoryImplementation<ServiceEntity, Long>{}
