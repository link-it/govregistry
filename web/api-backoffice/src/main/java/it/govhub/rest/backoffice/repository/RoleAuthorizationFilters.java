package it.govhub.rest.backoffice.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import it.govhub.rest.backoffice.entity.RoleAuthorizationEntity;
import it.govhub.rest.backoffice.entity.RoleAuthorizationEntity_;
import it.govhub.rest.backoffice.entity.UserEntity_;

public class RoleAuthorizationFilters {
	
	private RoleAuthorizationFilters() {}

	public static Specification<RoleAuthorizationEntity> empty() {
		return (Root<RoleAuthorizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
	}
	
	
	// Facciamo il confronto case insensitive portando in upperCase  i valori
	public static Specification<RoleAuthorizationEntity> byUser(Long userId) {
		return (Root<RoleAuthorizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.equal(root.get(RoleAuthorizationEntity_.user).get(UserEntity_.id), userId);
	}
	
}
