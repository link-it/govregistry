package it.govhub.govregistry.api;

import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.SpringDocConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.format.FormatterRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.MapperFeature;

import it.govhub.govregistry.commons.config.CommonsExportedBeans;
import it.govhub.govregistry.commons.config.TimeZoneConfigurer;
import it.govhub.govregistry.commons.exception.handlers.RequestRejectedExceptionHandler;
import it.govhub.govregistry.commons.utils.Base64String;
import it.govhub.govregistry.commons.utils.Base64StringSerializer;
import it.govhub.govregistry.readops.api.config.ReadOpsExportedBeans;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@Import({ ReadOpsExportedBeans.class, CommonsExportedBeans.class,   TimeZoneConfigurer.class })
@EnableJpaRepositories("it.govhub.govregistry.api.repository")
@ComponentScan( {"it.govhub.govregistry" })
public class Application  extends SpringBootServletInitializer {
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Value("${govhub.time-zone:Europe/Rome}")
	String timeZone;


	/**
	 * Gestisce la famiglia di header X-Forwarded o lo header Forwarded.
	 * Utile per la scrittura dei link hateoas in modo che tengano conto della presenza del proxy. 
	 */
	@Bean
	public ForwardedHeaderFilter forwardedHeaderFilter() {
		return new ForwardedHeaderFilter();
	}

	/**
	 * Modifichiamo il serializzatore JSON in modo da serializzare le Base64String
	 * come stringhe normali
	 */
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
		return builder ->  builder.
				timeZone(this.timeZone).
				serializerByType(Base64String.class, new Base64StringSerializer()).
				featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

	}

	/**
	 * Questa  serve per serializzare correttamente gli enum passati via
	 * parametro query. Altrimenti è necessario passarli in upperCase.
	 *
	 */
	@Configuration
	static class MyConfig implements WebMvcConfigurer {
		@Override
		public void addFormatters(FormatterRegistry registry) {
			ApplicationConversionService.configure(registry);
		}
	}
	
	
	/**
	 * Questo Bean Restituisce un Problem quando spring-security rifiuta una
	 * richiesta perchè ritenuta ad esempio non sicura.
	 */
	@Bean
	public RequestRejectedHandler requestRejectedHandler() {
	   return new RequestRejectedExceptionHandler();
	}
	
	
	/**
	 * Configurazione minimale per SpringDoc in modo che carichi gli asset sotto 
	 * src/main/resources/static
	 * 
	 */
	@Bean
	SpringDocConfiguration springDocConfiguration(){
	   return new SpringDocConfiguration();
	}
	@Bean
	SpringDocConfigProperties springDocConfigProperties() {
	   return new SpringDocConfigProperties();
	}

}