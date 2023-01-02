package it.govhub.govregistry.readops.api.config;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.Repository;

public interface ReadOnlyRepository<T, ID> extends Repository<T, ID>, JpaSpecificationExecutor<T> {
	
    Optional<T> findById(Long id);
    
    List<T> findAll();
    
    List<T> findAllById(Iterable<ID> ids);
}
