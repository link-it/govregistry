package it.govhub.rest.backoffice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import it.govhub.rest.backoffice.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long>{
	
	public Optional<UserEntity> findByPrincipal(String principal);

}
