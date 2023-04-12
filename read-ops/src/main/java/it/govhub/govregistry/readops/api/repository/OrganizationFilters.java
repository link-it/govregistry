/*
 * GovRegistry - Registries manager for GovHub
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
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

import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

import it.govhub.govregistry.commons.api.beans.OrganizationOrdering;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.OrganizationEntity_;
import it.govhub.govregistry.commons.exception.UnreachableException;

public class OrganizationFilters {
	
	private OrganizationFilters() {}
	
	public static Specification<OrganizationEntity> empty() {
		return (Root<OrganizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
	}
	
	
	public static Specification<OrganizationEntity> never() {
		return (Root<OrganizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> cb.isTrue(cb.literal(false)); 
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
	
	
	public static Sort sort(OrganizationOrdering sort, Direction sortDirection) {

		switch (sort) {
		case ID:
			return Sort.by(sortDirection, OrganizationEntity_.ID);
		case LEGAL_NAME:
			return Sort.by(sortDirection, OrganizationEntity_.LEGAL_NAME);
		case UNSORTED:
			return Sort.unsorted();
		default:
			throw new UnreachableException();		
		}
	}

	public static Specification<OrganizationEntity> byId(Collection<Long> orgIds) {
		return (Root<OrganizationEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->			
			root.get(OrganizationEntity_.id).in(orgIds);
	}

}
