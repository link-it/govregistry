package it.govhub.govregistry.readops.api.repository;

import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.config.ReadOnlyRepository;

public interface ReadUserRepository extends ReadOnlyRepository<UserEntity, Long> {
}
