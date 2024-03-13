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
package it.govhub.govregistry.commons.messages;

import java.util.Collection;

public class RestEntityMessageBuilder {

	private String entityName;
	
	
	public RestEntityMessageBuilder(String entityName) {
		this.entityName = entityName;
	}
	
	
	public String notFound(String key, Object keyValue) {
		return this.entityName + " with " + key + " [" + keyValue + "] Not Found."; 
	}
	
	public String conflict() {
		return this.entityName + " already present";
	}
	
	
	public String conflict(String key, Object keyValue) {
		return this.entityName + " with " + key + " [" + keyValue + "] Already Exists.";
	}
	
	public String idNotFound(Long id) {
		return notFound("id", id);
	}
	
	public String idsNotFound(Collection<Long> ids) {
		return this.entityName + " with ids IN [" + ids + "] Not Found.";
	}
	
	public String fieldNotModificable(String key) {
		return this.entityName + " field ["+key+"] is not modificable.";
	}
	
}
