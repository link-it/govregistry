package it.govhub.security.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import it.govhub.security.cache.PrincipalCacheTTLEvicter;

@Configuration
@ComponentScan( {"it.govhub.security.services"})
@EnableJpaRepositories( {"it.govhub.security.repository"})
@Import( PrincipalCacheTTLEvicter.class)
public class SecurityExportedBeans {}
