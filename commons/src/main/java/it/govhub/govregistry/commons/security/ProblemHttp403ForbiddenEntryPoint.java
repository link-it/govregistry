package it.govhub.govregistry.commons.security;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govhub.govregistry.commons.exception.RestResponseEntityExceptionHandler;
import it.govhub.govregistry.commons.exception.UnreachableException;

public class ProblemHttp403ForbiddenEntryPoint implements AuthenticationEntryPoint {
	
	private ObjectMapper jsonMapper;
	
	public ProblemHttp403ForbiddenEntryPoint(ObjectMapper mapper) {
		this.jsonMapper = mapper;
	}
	
	
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, 	AuthenticationException authException) throws IOException, ServletException {
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



}
