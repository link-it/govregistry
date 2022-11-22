package it.govhub.rest.backoffice.repository;

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

import it.govhub.rest.backoffice.config.SecurityConfig;
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
	
	
	public static Specification<RoleAuthorizationEntity> byAdmin(Long userId) {
		return (Root<RoleAuthorizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
		
		RoleAuthorizationFilters.byRoleName(SecurityConfig.RUOLO_GOVHUB_SYSADMIN)
			.and(RoleAuthorizationFilters.byUser(userId))
			.and(RoleAuthorizationFilters.expiresAfter(OffsetDateTime.now()))
			.toPredicate(root, query, cb);
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

	
	/**
	 * Specifica le Autorizzazioni che consentono di lavorare sul servizio argomento.
	 * 
	 * Se l'autorizzazione non specifica servizi, allora può lavorare su tutti. 
	 * 
	 */
	public static Specification<RoleAuthorizationEntity> onService(Long id) {
		return (Root<RoleAuthorizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.or(
					cb.isEmpty(root.get(RoleAuthorizationEntity_.services)),
					cb.literal(id).in(root.join(RoleAuthorizationEntity_.services).get(ServiceEntity_.id)));
	}
	
	
	/**
	 * Specifica le Autorizzazioni che consentono di lavorare sull'organizzazione argomento.
	 * 
	 * Se l'autorizzazione non specifica organizzazioni, allora può lavorare su tutte. 
	 * 
	 */
	public static Specification<RoleAuthorizationEntity> onOrganization(Long id) {
		return (Root<RoleAuthorizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.or(
					cb.isEmpty(root.get(RoleAuthorizationEntity_.organizations)),
					cb.literal(id).in(root.join(RoleAuthorizationEntity_.organizations, JoinType.LEFT).get(OrganizationEntity_.id)));
	}

	
	
	/**
	 * Filtra sui servizi autorizzati. Se la lista argomento è vuota, deve essere vuota anche quella sul db.
	 * 
	 */
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

	
	/**
	 * Filtra sulle organizzazioni autorizzate. Se la lista argomento è vuota, deve essere vuota anche quella sul db.
	 * 
	 */
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
