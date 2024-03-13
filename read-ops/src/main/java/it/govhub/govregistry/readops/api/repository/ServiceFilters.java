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


import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

import it.govhub.govregistry.commons.api.beans.ServiceOrdering;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity_;
import it.govhub.govregistry.commons.exception.UnreachableException;

public class ServiceFilters {
	
	private ServiceFilters() {}

	public static Specification<ServiceEntity> empty() {
		return (Root<ServiceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
	}
	
	public static Specification<ServiceEntity> never() {
		return (Root<ServiceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> cb.isTrue(cb.literal(false)); 
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
	
	
	public static Specification<ServiceEntity> byId(Collection<Long> serviceIds) {
		return (Root<ServiceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			root.get(ServiceEntity_.id).in(serviceIds);
	}
		
	
	public static Sort sort(ServiceOrdering sort, Direction direction) {
		
		switch (sort) {
		case SERVICE_NAME:
			return Sort.by(direction, ServiceEntity_.NAME);
		case ID:
			return Sort.by(direction, ServiceEntity_.ID);
		case UNSORTED:
			return Sort.unsorted();
		default:
			throw new UnreachableException();
		}
	}


}
