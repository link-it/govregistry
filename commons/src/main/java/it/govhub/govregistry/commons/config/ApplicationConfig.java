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
package it.govhub.govregistry.commons.config;

import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Bean che le singole applicazioni implementano e che descrive la configurazione necessaria alla parte
 * comune delle applicazioni. 
 *
 */
@Component
public interface ApplicationConfig {

	public String getApplicationId();
	
	public Set<String> getReadServiceRoles();
	
	public Set<String> getReadOrganizationRoles();
}
