package it.govhub.govregistry.readops.api.repository;

import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.readops.api.config.ReadOnlyRepository;

public interface ReadServiceRepository extends ReadOnlyRepository<ServiceEntity, Long> {

}
