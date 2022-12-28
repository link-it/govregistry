package it.govhub.govregistry.commons.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan( {"it.govhub.govregistry.commons" })
@EntityScan("it.govhub.govregistry.commons.entity")
public class CommonsExportedBeans {}
