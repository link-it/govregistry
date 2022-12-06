package it.govhub.govregistry.api.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan( {"it.govhub.govregistry.api"})
@EnableJpaRepositories("it.govhub.govregistry.api.repository")
@EntityScan("it.govhub.govregistry.api.entity")
public class SharedConfiguration {}
