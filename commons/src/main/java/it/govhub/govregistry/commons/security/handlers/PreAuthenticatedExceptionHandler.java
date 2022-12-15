package it.govhub.govregistry.commons.security.handlers;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;


/**
 * Questo filtro cattura le eccezioni sollevate durante la SecurityFilterChain di Spring.
 * 
 * Fa in modo di delegare al @ControllerAdvice (Il nostro RestResponseEntityExceptionHandler) la gestione delle eccezioni. 
 *
 */
@Component
public class PreAuthenticatedExceptionHandler extends OncePerRequestFilter {

	   private final Logger log = LoggerFactory.getLogger(getClass());

	    @Autowired
	    @Qualifier("handlerExceptionResolver")
	    private HandlerExceptionResolver resolver;

	    @Override
	    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
	            throws ServletException, IOException {

	        try {
	            filterChain.doFilter(request, response);
	        } catch (Exception e) {
	            log.error("Spring Security Filter Chain Exception:", e);
	            resolver.resolveException(request, response, null, e);
	        }
	    }
}
