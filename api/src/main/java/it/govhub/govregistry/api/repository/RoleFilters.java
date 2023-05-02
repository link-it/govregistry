package it.govhub.govregistry.api.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import it.govhub.govregistry.commons.entity.ApplicationEntity_;
import it.govhub.govregistry.commons.entity.RoleEntity;
import it.govhub.govregistry.commons.entity.RoleEntity_;

public class RoleFilters {

	private RoleFilters() {}

	public static Specification<RoleEntity> empty() {
		return (Root<RoleEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
	}
	
	
	public static Specification<RoleEntity> byApplicationId(String appId) {
		return (Root<RoleEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.equal(root.get(RoleEntity_.govhubApplication).get(ApplicationEntity_.applicationId), appId);
	}

	public static Specification<RoleEntity> likeRoleName(String q) {
		return (Root<RoleEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.like(
					cb.lower(root.get(RoleEntity_.name)),
					"%" + q.toLowerCase() + "%"
				);
	}
}
