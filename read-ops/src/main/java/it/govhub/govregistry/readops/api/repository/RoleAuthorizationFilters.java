/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govhub.govregistry.readops.api.repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;

import it.govhub.govregistry.commons.entity.OrganizationEntity_;
import it.govhub.govregistry.commons.entity.RoleAuthorizationEntity;
import it.govhub.govregistry.commons.entity.RoleAuthorizationEntity_;
import it.govhub.govregistry.commons.entity.RoleEntity_;
import it.govhub.govregistry.commons.entity.ServiceEntity_;
import it.govhub.govregistry.commons.entity.UserEntity_;

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
					cb.isNull(root.get(RoleAuthorizationEntity_.expirationDate)),
					cb.greaterThanOrEqualTo(root.get(RoleAuthorizationEntity_.expirationDate), date));
	}
	
	
	public static Specification<RoleAuthorizationEntity> byAssignableRole(Long role) {
		return (Root<RoleAuthorizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.literal(role).in(
					root.join(RoleAuthorizationEntity_.role).join(RoleEntity_.assignableRoles, JoinType.LEFT).get(RoleEntity_.id)
					);
	}

	
	public static Specification<RoleAuthorizationEntity> onService(Long id) {
		return (Root<RoleAuthorizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.or(
					cb.isEmpty(root.get(RoleAuthorizationEntity_.services)),
					cb.literal(id).in(root.join(RoleAuthorizationEntity_.services, JoinType.LEFT).get(ServiceEntity_.id)));
	}
	
	
	public static Specification<RoleAuthorizationEntity> onOrganization(Long id) {
		return (Root<RoleAuthorizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.or(
					cb.isEmpty(root.get(RoleAuthorizationEntity_.organizations)),
					cb.literal(id).in(root.join(RoleAuthorizationEntity_.organizations, JoinType.LEFT).get(OrganizationEntity_.id)));
	}

	
	
	public static Specification<RoleAuthorizationEntity> onServices(List<Long> services) {
		return (Root<RoleAuthorizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {

			Predicate emptyServicePred = cb.isEmpty(root.get(RoleAuthorizationEntity_.services));
			
			if (services.isEmpty()) {
				return emptyServicePred;
			} else {

				Subquery<Long> subQuery = query.subquery(Long.class);
				Root<RoleAuthorizationEntity> subQueryRoot = subQuery.correlate(root);
						
				subQuery.select(subQueryRoot.join(RoleAuthorizationEntity_.services).get(ServiceEntity_.id));
				
				ArrayList<Predicate> preds = new ArrayList<>();
				for (Long id : services) {
					preds.add(cb.in(cb.literal(id)).value(subQuery));
				}
				
				return cb.or(emptyServicePred, cb.and(preds.toArray(new Predicate[0])));
			}
		};

	}

	
	public static Specification<RoleAuthorizationEntity> onOrganizations(List<Long> organizations) {
		return (Root<RoleAuthorizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
			
			Predicate emptyOrgPred = cb.isEmpty(root.get(RoleAuthorizationEntity_.organizations));
			
			if (organizations.isEmpty()) {
				return emptyOrgPred;
			} else {

				Subquery<Long> subQuery = query.subquery(Long.class);
				Root<RoleAuthorizationEntity> subQueryRoot = subQuery.correlate(root);
						
				subQuery.select(subQueryRoot.join(RoleAuthorizationEntity_.organizations).get(OrganizationEntity_.id));
				
				ArrayList<Predicate> preds = new ArrayList<>();
				for (Long id : organizations) {
					preds.add(cb.in(cb.literal(id)).value(subQuery));
				}
				
				return cb.or(emptyOrgPred, cb.and(preds.toArray(new Predicate[0])));
			}
		};
	}

}
