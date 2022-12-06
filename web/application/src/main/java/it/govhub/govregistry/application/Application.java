package it.govhub.govregistry.application;

import java.sql.SQLException;

import org.h2.tools.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.format.FormatterRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import it.govhub.govregistry.api.config.Caches;
import it.govhub.govregistry.api.config.SharedConfiguration;
import it.govhub.govregistry.api.utils.Base64String;
import it.govhub.govregistry.api.utils.Base64StringSerializer;

@SpringBootApplication
@EnableScheduling
@Import({ SharedConfiguration.class, Caches.class })
public class Application extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	
	@Bean(initMethod = "start", destroyMethod = "stop")
	public Server inMemoryH2DatabaseServer() throws SQLException {
	    return Server.createTcpServer(
	      "-tcp", "-tcpAllowOthers", "-tcpPort", "9090");
	}

	@Bean
	public ForwardedHeaderFilter forwardedHeaderFilter() {
		return new ForwardedHeaderFilter();
	}

	/**
	 * Modifichiamo il serializzatore JSON in modo da serializza le Base64String
	 * come stringhe normali
	 */
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
		return builder -> builder.serializerByType(Base64String.class, new Base64StringSerializer());
	}

	/**
	 * Questa classe serve per serializzare correttamente gli enum passati via
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
	
	
}