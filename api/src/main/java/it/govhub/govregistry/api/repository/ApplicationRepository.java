package it.govhub.govregistry.api.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govregistry.commons.entity.ApplicationEntity;

public interface ApplicationRepository  extends JpaRepositoryImplementation<ApplicationEntity, Long> {

}
