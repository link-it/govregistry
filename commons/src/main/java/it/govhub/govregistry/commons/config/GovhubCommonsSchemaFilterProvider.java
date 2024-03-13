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

import org.hibernate.tool.schema.spi.SchemaFilter;
import org.hibernate.tool.schema.spi.SchemaFilterProvider;


/**
 * Le applicazioni spring che includono commons, producono nel ddl anche gli schemi per le entità di govregistry, perchè sono in commons.
 * 
 *  Includendo questo filtro, si dice ad hibernate di skippare quelle tabelle. Il filtro lo si installa mettendo questa property:
 *  
 *  	spring.jpa.properties.hibernate.hbm2ddl.schema_filter_provider=it.govhub.govregistry.commons.config.GovioCommonsSchemaFilterProvider
 *
 */
public class GovhubCommonsSchemaFilterProvider implements SchemaFilterProvider {

	@Override
	public SchemaFilter getCreateFilter() {
		return GovhubCommonsSchemaFilter.INSTANCE;
	}

	@Override
	public SchemaFilter getDropFilter() {
		return GovhubCommonsSchemaFilter.INSTANCE;
	}

	@Override
	public SchemaFilter getMigrateFilter() {
		return GovhubCommonsSchemaFilter.INSTANCE;
	}

	@Override
	public SchemaFilter getValidateFilter() {
		return GovhubCommonsSchemaFilter.INSTANCE;
	}

}
