package it.govhub.rest.backoffice.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.rest.backoffice.entity.ServiceEntity;

public interface ServiceRepository  extends JpaRepositoryImplementation<ServiceEntity, Long>{}
