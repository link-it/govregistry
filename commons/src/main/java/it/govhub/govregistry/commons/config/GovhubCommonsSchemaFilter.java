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

import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.mapping.Table;
import org.hibernate.tool.schema.spi.SchemaFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GovhubCommonsSchemaFilter implements SchemaFilter {
	
	public static final GovhubCommonsSchemaFilter INSTANCE = new GovhubCommonsSchemaFilter();
	
	private static final Set<String> tablesToExclude = Set.of(
			"govhub_applications",
			"govhub_organizations",
			"govhub_authorizations",
			"govhub_roles",
			"govhub_services",
			"govhub_users",
			"govhub_assignable_roles",
			"govhub_auth_organizations",
			"govhub_auth_services"
			);
	
	private static final Set<String> sequenceToExclude = Set.of(
			"seq_govhub_applications",
			"seq_govhub_organizations",
			"seq_govhub_authorizations",
			"seq_govhub_roles",
			"seq_govhub_services",
			"seq_govhub_users"
			);
	
	private Logger log = LoggerFactory.getLogger(GovhubCommonsSchemaFilter.class);
	
	
    @Override
    public boolean includeNamespace(Namespace namespace) {
        return true;
    }

    @Override
    public boolean includeTable(Table table) {
    	this.log.debug("Should include table {} ?", table.getName());
    	return ! tablesToExclude.contains(table.getName());
    }

	@Override
	public boolean includeSequence(org.hibernate.boot.model.relational.Sequence sequence) {
		this.log.debug("Should include sequence {} ?", sequence.getName().getSequenceName().getCanonicalName());
		return ! sequenceToExclude.contains(sequence.getName().getSequenceName().getCanonicalName());
	}


}
