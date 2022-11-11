package it.govhub.rest.backoffice.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import it.govhub.rest.backoffice.beans.OrganizationOrdering;
import it.govhub.rest.backoffice.entity.OrganizationEntity;
import it.govhub.rest.backoffice.entity.OrganizationEntity_;
import it.govhub.rest.backoffice.exception.UnreachableException;

public class OrganizationFilters {
	
	private OrganizationFilters() {}
	
	public static Specification<OrganizationEntity> empty() {
		return (Root<OrganizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
	}
	
	
	// Facciamo il confronto case insensitive portando in upperCase  i valori
	public static Specification<OrganizationEntity> likeTaxCode(String taxCode) {
		return (Root<OrganizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.like(cb.upper(root.get(OrganizationEntity_.taxCode)), "%"+taxCode.toUpperCase()+"%");		
	}
	
	
	public static Specification<OrganizationEntity> likeLegalName(String legalName) {
		return (Root<OrganizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.like(cb.upper(root.get(OrganizationEntity_.legalName)), "%"+legalName.toUpperCase()+"%");		
	}

	public static Sort sort(OrganizationOrdering sort) {
		if (sort == null) {
			return Sort.unsorted();
		}
		
		switch (sort) {
		case ID:
			return Sort.by(Sort.Direction.ASC, OrganizationEntity_.ID);
		case LEGAL_NAME:
			return Sort.by(Sort.Direction.ASC, OrganizationEntity_.LEGAL_NAME);
		default:
			throw new UnreachableException();		
		}
	}
	
	
}
