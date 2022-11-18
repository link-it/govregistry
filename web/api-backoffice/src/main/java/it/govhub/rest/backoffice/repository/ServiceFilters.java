package it.govhub.rest.backoffice.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

import it.govhub.rest.backoffice.beans.ServiceOrdering;
import it.govhub.rest.backoffice.entity.ServiceEntity;
import it.govhub.rest.backoffice.entity.ServiceEntity_;
import it.govhub.rest.backoffice.exception.UnreachableException;

public class ServiceFilters {
	
	private ServiceFilters() {}

	public static Specification<ServiceEntity> empty() {
		return (Root<ServiceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
	}
	
	
	// Facciamo il confronto case insensitive portando in upperCase i valori
	
	public static Specification<ServiceEntity> likeName(String name) {
		return (Root<ServiceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.like(cb.upper(root.get(ServiceEntity_.name)), "%"+name.toUpperCase()+"%");		
	}
	
	
	public static Specification<ServiceEntity> likeDescription(String description) {
		return (Root<ServiceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.like(cb.upper(root.get(ServiceEntity_.description)), "%"+description.toUpperCase()+"%");		
	}		
		
	
	public static Sort sort(ServiceOrdering sort, Direction direction) {
		
		if (sort == null) {
			return Sort.unsorted();
		}
		
		switch (sort) {
		case SERVICE_NAME:
			return Sort.by(direction, ServiceEntity_.NAME);
		case ID:
			return Sort.by(direction, ServiceEntity_.ID);
		default:
			throw new UnreachableException();
		}
	}
}
