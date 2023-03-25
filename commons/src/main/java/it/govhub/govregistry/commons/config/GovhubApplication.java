package it.govhub.govregistry.commons.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.MapperFeature;

import it.govhub.govregistry.commons.exception.handlers.RequestRejectedExceptionHandler;
import it.govhub.govregistry.commons.utils.Base64String;
import it.govhub.govregistry.commons.utils.Base64StringSerializer;

//@Import({ CommonsExportedBeans.class,   TimeZoneConfigurer.class })
public class GovhubApplication /*extends SpringBootServletInitializer*/ {
	
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
	
	
}