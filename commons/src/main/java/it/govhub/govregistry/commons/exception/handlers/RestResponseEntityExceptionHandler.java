/*
 * GovRegistry - Registries manager for GovHub
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
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
import it.govhub.govregistry.commons.exception.InternalException;
import it.govhub.govregistry.commons.exception.NotAuthorizedException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.exception.InternalConfigurationException;
import it.govhub.govregistry.commons.exception.SemanticValidationException;
import it.govhub.govregistry.commons.exception.UnreachableException;
import it.govhub.govregistry.commons.messages.SystemMessages;
import it.govhub.govregistry.commons.utils.RequestUtils;


@ControllerAdvice
@ResponseBody
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
	
	public static Object buildProblem(HttpStatus status, String detail, String accept) {
		
		   List<MediaType> mediaTypes = MediaType.parseMediaTypes(accept);
		   if (mediaTypes.isEmpty() || mediaTypes.contains(MediaType.ALL )  || mediaTypes.contains(MediaType.APPLICATION_JSON) || mediaTypes.contains(MediaType.APPLICATION_PROBLEM_JSON)) {
				try {
					Problem ret = new Problem();
					ret.setStatus(status.value());
					ret.setTitle(status.getReasonPhrase());
					ret.setType(new URI(problemTypes.get(status)));
					ret.setDetail(detail);
					return ret;
				} catch (URISyntaxException e){
					// Non deve mai fallire la new URI di sopra
					throw new UnreachableException(e);
				}
		   }
		else {
			return "HTTP " + status.value() + ": " + detail;
		}
	}
	
	public static Problem buildProblem(HttpStatus status, String detail) {
		return (Problem) buildProblem(status, detail, null);
	}


	@ResponseStatus(HttpStatus.CONFLICT)
	@ExceptionHandler(ConflictException.class)
	public Object handleConstraintViolation(ConflictException ex, WebRequest request) {		
		return buildProblem(HttpStatus.CONFLICT, ex.getLocalizedMessage(), request.getHeader(HttpHeaders.ACCEPT) );
	}
	
	
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(ResourceNotFoundException.class)
	public Object handleConstraintViolation(ResourceNotFoundException ex, WebRequest request) {
		return buildProblem(HttpStatus.NOT_FOUND, ex.getLocalizedMessage(), request.getHeader(HttpHeaders.ACCEPT));
	}
	
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler({BadRequestException.class, MethodArgumentTypeMismatchException.class, ConstraintViolationException.class})
	public Object handleConstraintViolation(RuntimeException ex, WebRequest request ) {		
		return buildProblem(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), request.getHeader(HttpHeaders.ACCEPT));
	}

	
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	@ExceptionHandler(SemanticValidationException.class)
	public Object handleConstraintViolation(SemanticValidationException ex, WebRequest request) {
		return buildProblem(HttpStatus.UNPROCESSABLE_ENTITY, ex.getLocalizedMessage(), request.getHeader(HttpHeaders.ACCEPT));
	}
	
	
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(NotAuthorizedException.class)
	public Object handleConstraintViolation(NotAuthorizedException ex, WebRequest request) {
		return buildProblem(HttpStatus.UNAUTHORIZED, ex.getLocalizedMessage(), request.getHeader(HttpHeaders.ACCEPT));
	}
	
	
	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ExceptionHandler(ForbiddenException.class)
	public Object handleConstraintViolation(ForbiddenException ex, WebRequest request) {
		return buildProblem(HttpStatus.FORBIDDEN, ex.getLocalizedMessage(),request.getHeader(HttpHeaders.ACCEPT));
	}
	
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({InternalConfigurationException.class})
	public final Object handleInternalConfigurationException(InternalConfigurationException ex, WebRequest request) {
		logger.error("An Internal Configuration Exception was Raised:: {}", ex);
		return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage(), request.getHeader(HttpHeaders.ACCEPT)) ;
	}
	
	
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({InternalException.class})
	public final Object handleAllInternalExceptions(InternalException ex, WebRequest request) {
		logger.error("Handling Internal Server Error: {}", ex);
		return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, SystemMessages.internalError(), request.getHeader(HttpHeaders.ACCEPT)) ;
	}
	
	
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({RuntimeException.class})
	public final Object catchAll(RuntimeException ex, WebRequest request) {
		logger.error("Handling Uncaught Runtime Exception: {}", ex);
		return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, SystemMessages.internalError(), request.getHeader(HttpHeaders.ACCEPT));
	}
	
	
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({Exception.class})
	public final Object catchAll(Exception ex, WebRequest request) {
		logger.error("Handling Uncaught Exception: {}", ex);
		return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, SystemMessages.internalError(),request.getHeader(HttpHeaders.ACCEPT));
	}
	

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			org.springframework.http.HttpHeaders headers, HttpStatus status, WebRequest request) {

		var error = ex.getBindingResult().getAllErrors().get(0);

		return new ResponseEntity<>(
				buildProblem(HttpStatus.BAD_REQUEST, RequestUtils.extractValidationError(error),request.getHeader(HttpHeaders.ACCEPT)),
				HttpStatus.BAD_REQUEST);
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
		return new ResponseEntity<>(
				buildProblem(HttpStatus.BAD_REQUEST,msg, request.getHeader(HttpHeaders.ACCEPT)),
				HttpStatus.BAD_REQUEST);
	}

	
	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(
			NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

		return new ResponseEntity<>(
				buildProblem(HttpStatus.NOT_FOUND,ex.getLocalizedMessage(), request.getHeader(HttpHeaders.ACCEPT)),
				HttpStatus.NOT_FOUND);	
		}
	
	
	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
			HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

		return new ResponseEntity<>(
				buildProblem(HttpStatus.BAD_REQUEST,ex.getLocalizedMessage(), request.getHeader(HttpHeaders.ACCEPT)),
				HttpStatus.BAD_REQUEST);	
		}

	
	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(
			MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

		return new ResponseEntity<>(
				buildProblem(HttpStatus.BAD_REQUEST,ex.getLocalizedMessage(), request.getHeader(HttpHeaders.ACCEPT)),
				HttpStatus.BAD_REQUEST);
	}
	
	/**
	 * Quanto il client ci manda un header Accept non supportato.
	 * 
	 * 
	 */
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
		return new ResponseEntity<>(
				buildProblem(HttpStatus.INTERNAL_SERVER_ERROR,ex.getLocalizedMessage(), request.getHeader(HttpHeaders.ACCEPT)) ,
				HttpStatus.INTERNAL_SERVER_ERROR);
	}
	

}
