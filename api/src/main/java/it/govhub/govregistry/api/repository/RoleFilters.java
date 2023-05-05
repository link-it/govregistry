/*******************************************************************************
 *  GovRegistry - Registries manager for GovHub
 *  
 *  Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 3, as published by
 *  the Free Software Foundation.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  
 *******************************************************************************/
package it.govhub.govregistry.api.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import it.govhub.govregistry.commons.entity.ApplicationEntity_;
import it.govhub.govregistry.commons.entity.RoleEntity;
import it.govhub.govregistry.commons.entity.RoleEntity_;

public class RoleFilters {

	private RoleFilters() {}

	public static Specification<RoleEntity> empty() {
		return (Root<RoleEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
	}
	
	
	public static Specification<RoleEntity> byApplicationId(String appId) {
		return (Root<RoleEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.equal(root.get(RoleEntity_.govhubApplication).get(ApplicationEntity_.applicationId), appId);
	}

	public static Specification<RoleEntity> likeRoleName(String q) {
		return (Root<RoleEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.like(
					cb.lower(root.get(RoleEntity_.name)),
					"%" + q.toLowerCase() + "%"
				);
	}
}
