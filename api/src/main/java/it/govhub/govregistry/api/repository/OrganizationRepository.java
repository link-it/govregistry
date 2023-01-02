package it.govhub.govregistry.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govregistry.commons.entity.OrganizationEntity;

public interface OrganizationRepository extends JpaRepositoryImplementation<OrganizationEntity, Long> {
	public Optional<OrganizationEntity> findByTaxCode(String taxCode);
	
	public Optional<OrganizationEntity> findByLegalName(String legalName);

}
