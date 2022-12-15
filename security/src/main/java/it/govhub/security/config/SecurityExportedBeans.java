package it.govhub.security.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import it.govhub.security.caches.PrincipalCacheTTLEvicter;

@Configuration
@ComponentScan( {"it.govhub.security.services"})
@Import( PrincipalCacheTTLEvicter.class)
public class SecurityExportedBeans {}
