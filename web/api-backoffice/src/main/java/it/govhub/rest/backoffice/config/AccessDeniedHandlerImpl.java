package it.govhub.rest.backoffice.config;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

	private ObjectMapper jsonMapper;
	
	public AccessDeniedHandlerImpl(ObjectMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}
	
	
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		
		AuthenticationProblem problem = new AuthenticationProblem();
		
		problem.status = HttpStatus.FORBIDDEN.value();
		problem.title = HttpStatus.FORBIDDEN.getReasonPhrase();
		problem.detail = accessDeniedException.getMessage();
		
		// imposto il content-type della risposta
		response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(problem.status);
		
		ServletOutputStream outputStream = null;
		try{
			problem.instance = new URI("https://www.rfc-editor.org/rfc/rfc9110.html#name-401-unauthorized");
			outputStream = response.getOutputStream();
			this.jsonMapper.writeValue(outputStream, problem);
			outputStream.flush();
		}catch(Exception e) {

		} finally {
		}		
	}

}
