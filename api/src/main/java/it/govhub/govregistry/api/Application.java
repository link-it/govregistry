package it.govhub.govregistry.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import it.govhub.govregistry.commons.config.GovhubApplication;
import it.govhub.govregistry.readops.api.config.ReadOpsExportedBeans;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@Import({ ReadOpsExportedBeans.class })
@EnableJpaRepositories("it.govhub.govregistry.api.repository")
@ComponentScan( {"it.govhub.govregistry" })
public class Application extends GovhubApplication {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(Application.class);
    }
	
}