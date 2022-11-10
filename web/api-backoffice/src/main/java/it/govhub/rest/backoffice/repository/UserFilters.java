package it.govhub.rest.backoffice.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.entity.UserEntity_;

public class UserFilters {

	private UserFilters() {}
	
	public static Specification<UserEntity> empty() {
		return (Root<UserEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
	}
	
	// Facciamo il confronto case insensitive portando in upperCase i valori
	public static Specification<UserEntity> likePrincipal(String principal) {
		return (Root<UserEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.like(cb.upper(root.get(UserEntity_.principal)), "%"+principal.toUpperCase()+"%");		
	}
	
	
	public static Specification<UserEntity> likeFullName(String fullName) {
		return (Root<UserEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.like(cb.upper(root.get(UserEntity_.fullName)), "%"+fullName.toUpperCase()+"%");
	}
	
	
	public static Specification<UserEntity> byEnabled(Boolean enabled) {
		return (Root<UserEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.equal(root.get(UserEntity_.enabled),enabled);		
	}
	
	
}
