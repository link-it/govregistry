package it.govhub.govregistry.commons.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Offre la stessa funzionalità dell'annotazione @RestController, in più
 * aggiunge il contesto /v1 a tutti i metodi del controller
 * 
 * @author Francesco Scarlato
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Controller
@ResponseBody
@RequestMapping("/v1")
public @interface V1RestController {

    @AliasFor(annotation = Component.class)
    String value() default "";
}

