package it.govhub.govregistry.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import it.govhub.govregistry.api.utils.Base64String;
import it.govhub.govregistry.api.utils.Base64StringSerializer;

	//@SpringBootApplication
	//@EnableScheduling
	public class Application /*extends SpringBootServletInitializer*/ {
/*
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	
	@Bean
	public ForwardedHeaderFilter forwardedHeaderFilter() {
	    return new ForwardedHeaderFilter();
	}
		

	@Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder
        		.serializerByType(Base64String.class, new Base64StringSerializer());
    }
	
	
	 @Configuration
    static class MyConfig implements  WebMvcConfigurer {
        @Override
        public void addFormatters(FormatterRegistry registry) {
            ApplicationConversionService.configure(registry);
           }
    }*/
}