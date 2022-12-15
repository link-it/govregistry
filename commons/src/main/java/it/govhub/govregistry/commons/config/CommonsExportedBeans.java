package it.govhub.govregistry.commons.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan( {"it.govhub.govregistry.commons", "it.govhub.security.services"})
@EnableJpaRepositories("it.govhub.govregistry.commons.repository")
@EntityScan("it.govhub.govregistry.commons.entity")
public class CommonsExportedBeans {}
