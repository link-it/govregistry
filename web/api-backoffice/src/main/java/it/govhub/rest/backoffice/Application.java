package it.govhub.rest.backoffice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.ForwardedHeaderFilter;

	@SpringBootApplication
	public class Application extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	
	@Bean
	public ForwardedHeaderFilter forwardedHeaderFilter() {
	    return new ForwardedHeaderFilter();
	}
		

	/**
	 * Modifichiamo il serializzatore JSON in due punti:
	 * 	- Non serializzare proprietà null
	 * 	- Serializza le Base64String come stringhe normali
	 */
    /*@Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
    	
        return builder -> builder.serializationInclusion(Include.NON_NULL);
//        		.serializerByType(Base64String.class, new Base64StringSerializer());
    }*/
	
}