package it.govhub.govregistry.readops.api.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan( {"it.govhub.govregistry.readops.api"})
@EnableJpaRepositories("it.govhub.govregistry.readops.api.repository")
public class ReadOpsExportedBeans {}
