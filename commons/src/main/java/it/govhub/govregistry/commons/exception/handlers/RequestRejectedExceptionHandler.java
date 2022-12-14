package it.govhub.govregistry.commons.exception.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.security.web.firewall.RequestRejectedHandler;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.govhub.govregistry.commons.beans.Problem;

public class RequestRejectedExceptionHandler implements RequestRejectedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, RequestRejectedException ex) throws IOException, ServletException {
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		
		Problem p = RestResponseEntityExceptionHandler.buildProblem(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
		
		ObjectMapper jm = new ObjectMapper();
		jm.setSerializationInclusion(Include.NON_NULL);
		response.getOutputStream().write(jm.writeValueAsBytes(p));
	}

}
