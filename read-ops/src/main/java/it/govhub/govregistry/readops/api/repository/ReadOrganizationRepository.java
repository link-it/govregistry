package it.govhub.govregistry.readops.api.repository;

import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.readops.api.config.ReadOnlyRepository;

public interface ReadOrganizationRepository extends ReadOnlyRepository<OrganizationEntity, Long> {
}
