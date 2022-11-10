package it.govhub.rest.backoffice.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import it.govhub.rest.backoffice.entity.OrganizationEntity;
import it.govhub.rest.backoffice.entity.OrganizationEntity_;

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
	
	
}
