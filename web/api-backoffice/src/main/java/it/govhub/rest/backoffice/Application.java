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
	
	
	/*@Bean
	public MethodValidationPostProcessor methodValidationPostProcessor() {
		MethodValidationPostProcessor ret = new MethodValidationPostProcessor();
		
		// BUGFIX Mappatura RestController: 
		// https://github.com/spring-projects/spring-boot/issues/17000
		//
		// I RestController che implementano un'interfaccia	e che hanno @Validated come annotazione
		// non venivano mappati sugli endpoint, con setProxyTargetClass(true) invece si.
		
		ret.setProxyTargetClass(true);
		
		return ret;
	}*/
	

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