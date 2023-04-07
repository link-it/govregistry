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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

import it.govhub.govregistry.commons.api.beans.UserOrdering;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.entity.UserEntity_;
import it.govhub.govregistry.commons.exception.UnreachableException;

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
	
	

	public static Sort sort(UserOrdering sort, Direction direction) {
		
		switch (sort) {
		case FULL_NAME:
			return Sort.by(direction, UserEntity_.FULL_NAME);
		case ID:
			return Sort.by(direction, UserEntity_.ID);
		case UNSORTED:
			return Sort.unsorted();
		default:
			throw new UnreachableException();
		}
	}

}
