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
