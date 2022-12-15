package it.govhub.govregistry.commons.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govregistry.commons.entity.ServiceEntity;

public interface ServiceRepository  extends JpaRepositoryImplementation<ServiceEntity, Long>{}
