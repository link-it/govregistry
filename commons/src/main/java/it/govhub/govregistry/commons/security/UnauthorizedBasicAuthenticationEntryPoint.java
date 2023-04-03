package it.govhub.govregistry.commons.security;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govhub.govregistry.commons.beans.AuthenticationProblem;
import it.govhub.govregistry.commons.exception.UnreachableException;
import it.govhub.govregistry.commons.exception.handlers.RestResponseEntityExceptionHandler;

@Component
public class UnauthorizedBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {
	
	private ObjectMapper jsonMapper;
	
	Logger log = LoggerFactory.getLogger(UnauthorizedBasicAuthenticationEntryPoint.class);

	public UnauthorizedBasicAuthenticationEntryPoint(ObjectMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

    @Override
    public void commence(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        
    	response.addHeader("WWW-Authenticate", "Basic realm=\"" + getRealmName() + "\"");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        log.debug("Mappo la AuthenticationException in un problem: {}", authException.getMessage());
		
		AuthenticationProblem problem = new AuthenticationProblem();
		problem.status = HttpStatus.UNAUTHORIZED.value();
		problem.title = HttpStatus.UNAUTHORIZED.getReasonPhrase();
		problem.detail = authException.getMessage();
		
		// imposto il content-type della risposta
		response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(problem.status);
		
		ServletOutputStream outputStream = null;
		try{
			problem.instance = new URI(RestResponseEntityExceptionHandler.problemTypes.get(HttpStatus.UNAUTHORIZED));
			outputStream = response.getOutputStream();
			this.jsonMapper.writeValue(outputStream, problem);
			outputStream.flush();
		} catch(Exception e) {
			throw new UnreachableException(e);
		}
    }

    @Override
    public void afterPropertiesSet() {
    	// TODO
        setRealmName("Govio");
        super.afterPropertiesSet();
    }
}