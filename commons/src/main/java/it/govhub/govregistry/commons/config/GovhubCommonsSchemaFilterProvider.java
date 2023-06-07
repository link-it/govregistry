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
