package it.govhub.govregistry.commons.security.handlers;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govhub.govregistry.commons.beans.AuthenticationProblem;
import it.govhub.govregistry.commons.exception.UnreachableException;
import it.govhub.govregistry.commons.exception.handlers.RestResponseEntityExceptionHandler;

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

	@Autowired
	private ObjectMapper jsonMapper;
	
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		
		AuthenticationProblem problem = new AuthenticationProblem();
		
		problem.status = HttpStatus.FORBIDDEN.value();
		problem.title = HttpStatus.FORBIDDEN.getReasonPhrase();
		problem.detail = accessDeniedException.getMessage();
		
		// imposto il content-type della risposta
		response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.setStatus(problem.status);
		
		ServletOutputStream outputStream = null;
		try{
			problem.instance = new URI(RestResponseEntityExceptionHandler.problemTypes.get(HttpStatus.FORBIDDEN));
			outputStream = response.getOutputStream();
			this.jsonMapper.writeValue(outputStream, problem);
			outputStream.flush();
		}catch(Exception e) {
			throw new UnreachableException();
		}
	}

}