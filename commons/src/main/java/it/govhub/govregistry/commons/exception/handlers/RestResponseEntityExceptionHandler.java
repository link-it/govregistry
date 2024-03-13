/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govhub.govregistry.commons.exception.handlers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;

import it.govhub.govregistry.commons.api.beans.Problem;
import it.govhub.govregistry.commons.exception.BadRequestException;
import it.govhub.govregistry.commons.exception.ConflictException;
import it.govhub.govregistry.commons.exception.ForbiddenException;
import it.govhub.govregistry.commons.exception.InternalConfigurationException;
import it.govhub.govregistry.commons.exception.InternalException;
import it.govhub.govregistry.commons.exception.NotAuthorizedException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.exception.SemanticValidationException;
import it.govhub.govregistry.commons.exception.UnreachableException;
import it.govhub.govregistry.commons.messages.SystemMessages;
import it.govhub.govregistry.commons.utils.RequestUtils;


@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	
	public static final Map<HttpStatus, String> problemTypes = Map.ofEntries(
			new AbstractMap.SimpleEntry<HttpStatus, String>(HttpStatus.CONFLICT,  "https://www.rfc-editor.org/rfc/rfc9110.html#name-409-conflict"),
			new AbstractMap.SimpleEntry<HttpStatus, String>(HttpStatus.NOT_FOUND, "https://www.rfc-editor.org/rfc/rfc9110.html#name-404-not-found"),
			new AbstractMap.SimpleEntry<HttpStatus, String>(HttpStatus.BAD_REQUEST,"https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request"),
			new AbstractMap.SimpleEntry<HttpStatus, String>(HttpStatus.UNPROCESSABLE_ENTITY,"https://www.rfc-editor.org/rfc/rfc9110.html#name-422-unprocessable-content"),
			new AbstractMap.SimpleEntry<HttpStatus, String>(HttpStatus.INTERNAL_SERVER_ERROR, "https://www.rfc-editor.org/rfc/rfc9110.html#name-500-internal-server-error"),
			new AbstractMap.SimpleEntry<HttpStatus, String>(HttpStatus.OK, "https://www.rfc-editor.org/rfc/rfc9110.html#name-200-ok"),
			new AbstractMap.SimpleEntry<HttpStatus, String>(HttpStatus.UNAUTHORIZED, "https://www.rfc-editor.org/rfc/rfc9110.html#name-401-unauthorized"),
			new AbstractMap.SimpleEntry<HttpStatus, String>(HttpStatus.FORBIDDEN, "https://www.rfc-editor.org/rfc/rfc9110.html#name-403-forbidden"),
			new AbstractMap.SimpleEntry<HttpStatus, String>(HttpStatus.NOT_ACCEPTABLE, "https://www.rfc-editor.org/rfc/rfc9110.html#name-406-not-acceptable"),
			new AbstractMap.SimpleEntry<HttpStatus, String>(HttpStatus.BAD_GATEWAY, "https://www.rfc-editor.org/rfc/rfc9110.html#name-502-bad-gateway"),
			new AbstractMap.SimpleEntry<HttpStatus, String>(HttpStatus.SERVICE_UNAVAILABLE, "https://www.rfc-editor.org/rfc/rfc9110.html#name-503-service-unavailable"),
			new AbstractMap.SimpleEntry<HttpStatus, String>(HttpStatus.TOO_MANY_REQUESTS, "https://www.rfc-editor.org/rfc/rfc6585#section-4")
		);
	
	private Logger logger = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);
	
	public static ResponseEntity<Object> buildResponseProblem(HttpStatus status, String detail, String accept) {
		
		   List<MediaType> mediaTypes = MediaType.parseMediaTypes(accept);
		   if (mediaTypes.isEmpty() || mediaTypes.contains(MediaType.ALL )  || mediaTypes.contains(MediaType.APPLICATION_JSON) || mediaTypes.contains(MediaType.APPLICATION_PROBLEM_JSON)) {
					return ResponseEntity.
							status(status).
			                contentType(MediaType.APPLICATION_PROBLEM_JSON).
							body(buildProblem(status, detail));
		   }
		   else {
			   return ResponseEntity.
					   status(status).
					   body("HTTP " + status.value() + ": " + detail);
		}
	}
	
	public static Problem buildProblem(HttpStatus status, String detail) {
		Problem ret = new Problem();
		ret.setStatus(status.value());
		ret.setTitle(status.getReasonPhrase());
		try {
			ret.setType(new URI(problemTypes.get(status)));
		} catch (URISyntaxException e) {
			throw new UnreachableException(e);
		}
		ret.setDetail(detail);
		
		return ret;
	}


	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<Object> handleConstraintViolation(ConflictException ex, WebRequest request) {		
		return buildResponseProblem(HttpStatus.CONFLICT, ex.getLocalizedMessage(), request.getHeader(HttpHeaders.ACCEPT) );
	}
	
	
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Object> handleConstraintViolation(ResourceNotFoundException ex, WebRequest request) {
		return buildResponseProblem(HttpStatus.NOT_FOUND, ex.getLocalizedMessage(), request.getHeader(HttpHeaders.ACCEPT));
	}
	
	
	@ExceptionHandler({BadRequestException.class, MethodArgumentTypeMismatchException.class, ConstraintViolationException.class})
	public ResponseEntity<Object> handleConstraintViolation(RuntimeException ex, WebRequest request ) {		
		return buildResponseProblem(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), request.getHeader(HttpHeaders.ACCEPT));
	}

	
	@ExceptionHandler(SemanticValidationException.class)
	public ResponseEntity<Object> handleConstraintViolation(SemanticValidationException ex, WebRequest request) {
		return buildResponseProblem(HttpStatus.UNPROCESSABLE_ENTITY, ex.getLocalizedMessage(), request.getHeader(HttpHeaders.ACCEPT));
	}
	
	
	@ExceptionHandler(NotAuthorizedException.class)
	public ResponseEntity<Object> handleConstraintViolation(NotAuthorizedException ex, WebRequest request) {
		return buildResponseProblem(HttpStatus.UNAUTHORIZED, ex.getLocalizedMessage(), request.getHeader(HttpHeaders.ACCEPT));
	}
	
	
	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<Object> handleConstraintViolation(ForbiddenException ex, WebRequest request) {
		return buildResponseProblem(HttpStatus.FORBIDDEN, ex.getLocalizedMessage(),request.getHeader(HttpHeaders.ACCEPT));
	}
	
	@ExceptionHandler({InternalConfigurationException.class})
	public final ResponseEntity<Object> handleInternalConfigurationException(InternalConfigurationException ex, WebRequest request) {
		logger.error("An Internal Configuration Exception was Raised:: {}", ex);
		return buildResponseProblem(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage(), request.getHeader(HttpHeaders.ACCEPT)) ;
	}
	
	@ExceptionHandler({InternalException.class})
	public final ResponseEntity<Object> handleAllInternalExceptions(InternalException ex, WebRequest request) {
		logger.error("Handling Internal Server Error: {}", ex);
		return buildResponseProblem(HttpStatus.INTERNAL_SERVER_ERROR, SystemMessages.internalError(), request.getHeader(HttpHeaders.ACCEPT)) ;
	}
	
	
	@ExceptionHandler({RuntimeException.class})
	public final ResponseEntity<Object> catchAll(RuntimeException ex, WebRequest request) {
		logger.error("Handling Uncaught Runtime Exception: {}", ex);
		return buildResponseProblem(HttpStatus.INTERNAL_SERVER_ERROR, SystemMessages.internalError(), request.getHeader(HttpHeaders.ACCEPT));
	}
	
	
	@ExceptionHandler({Exception.class})
	public final ResponseEntity<Object> catchAll(Exception ex, WebRequest request) {
		logger.error("Handling Uncaught Exception: {}", ex);
		return buildResponseProblem(HttpStatus.INTERNAL_SERVER_ERROR, SystemMessages.internalError(),request.getHeader(HttpHeaders.ACCEPT));
	}
	

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			org.springframework.http.HttpHeaders headers, HttpStatus status, WebRequest request) {

		var error = ex.getBindingResult().getAllErrors().get(0);

		return 	buildResponseProblem(HttpStatus.BAD_REQUEST, RequestUtils.extractValidationError(error),request.getHeader(HttpHeaders.ACCEPT));
	}
	
	
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(
			HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		String msg;
		if (ex.getCause() instanceof ValueInstantiationException) {
			msg = ex.getCause().getLocalizedMessage();			
		} else {
			msg = ex.getLocalizedMessage();
		}
		return 	buildResponseProblem(HttpStatus.BAD_REQUEST,msg, request.getHeader(HttpHeaders.ACCEPT));
	}

	
	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(	NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		return 	buildResponseProblem(HttpStatus.NOT_FOUND,ex.getLocalizedMessage(), request.getHeader(HttpHeaders.ACCEPT));
		}
	
	
	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(	HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		return 	buildResponseProblem(HttpStatus.BAD_REQUEST,ex.getLocalizedMessage(), request.getHeader(HttpHeaders.ACCEPT));
		}

	
	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		return buildResponseProblem(HttpStatus.BAD_REQUEST,ex.getLocalizedMessage(), request.getHeader(HttpHeaders.ACCEPT));
	}
	
	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
			HttpMediaTypeNotAcceptableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		return new ResponseEntity<>(
				"acceptable MIME type:" + MediaType.APPLICATION_JSON_VALUE,
				HttpStatus.NOT_ACCEPTABLE);
	}
	
	
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(
			Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {

		if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
			request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, RequestAttributes.SCOPE_REQUEST);
		}
		return 	buildResponseProblem(HttpStatus.INTERNAL_SERVER_ERROR,ex.getLocalizedMessage(), request.getHeader(HttpHeaders.ACCEPT));
	}
	

}
