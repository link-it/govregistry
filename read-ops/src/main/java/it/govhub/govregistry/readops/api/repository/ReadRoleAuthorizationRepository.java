package it.govhub.govregistry.readops.api.repository;

import it.govhub.govregistry.commons.entity.RoleAuthorizationEntity;
import it.govhub.govregistry.readops.api.config.ReadOnlyRepository;

public interface ReadRoleAuthorizationRepository  extends ReadOnlyRepository<RoleAuthorizationEntity, Long> {
}
