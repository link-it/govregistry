package it.govhub.rest.backoffice.repository;

import java.time.OffsetDateTime;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import it.govhub.rest.backoffice.entity.OrganizationEntity_;
import it.govhub.rest.backoffice.entity.RoleAuthorizationEntity;
import it.govhub.rest.backoffice.entity.RoleAuthorizationEntity_;
import it.govhub.rest.backoffice.entity.RoleEntity_;
import it.govhub.rest.backoffice.entity.ServiceEntity_;
import it.govhub.rest.backoffice.entity.UserEntity_;

public class RoleAuthorizationFilters {
	
	private RoleAuthorizationFilters() {}

	public static Specification<RoleAuthorizationEntity> empty() {
		return (Root<RoleAuthorizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
	}
	
	
	public static Specification<RoleAuthorizationEntity> byUser(Long userId) {
		return (Root<RoleAuthorizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.equal(root.get(RoleAuthorizationEntity_.user).get(UserEntity_.id), userId);
	}
	
	
	public static Specification<RoleAuthorizationEntity> byRoleName(String... roles) {
		return (Root<RoleAuthorizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			root.get(RoleAuthorizationEntity_.role).get(RoleEntity_.name).in((Object[])roles);
	}	
	
	
	public static Specification<RoleAuthorizationEntity> expiresAfter(OffsetDateTime date) {
		return (Root<RoleAuthorizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.or(
					cb.greaterThanOrEqualTo(root.get(RoleAuthorizationEntity_.expirationDate), date),
					cb.isNull(root.get(RoleAuthorizationEntity_.expirationDate)));
	}

	
	public static Specification<RoleAuthorizationEntity> onService(Long id) {
		return (Root<RoleAuthorizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.literal(id).in(root.join(RoleAuthorizationEntity_.services).get(ServiceEntity_.id), id);
	}
	
	
	public static Specification<RoleAuthorizationEntity> onOrganization(Long id) {
		return (Root<RoleAuthorizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.literal(id).in(root.join(RoleAuthorizationEntity_.organizations).get(OrganizationEntity_.id), id);
	}
	
	
}
