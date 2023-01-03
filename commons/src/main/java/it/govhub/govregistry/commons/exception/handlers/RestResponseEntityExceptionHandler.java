package it.govhub.govregistry.commons.exception.handlers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

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
import it.govhub.govregistry.commons.exception.SemanticValidationException;
import it.govhub.govregistry.commons.exception.UnreachableException;
import it.govhub.govregistry.commons.utils.RequestUtils;


@ControllerAdvice
@ResponseBody
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	
	public static final Map<HttpStatus, String> problemTypes = Map.of(
			HttpStatus.CONFLICT,  "https://www.rfc-editor.org/rfc/rfc9110.html#name-409-conflict",
			HttpStatus.NOT_FOUND, "https://www.rfc-editor.org/rfc/rfc9110.html#name-404-not-found",
			HttpStatus.BAD_REQUEST,"https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request",
			HttpStatus.UNPROCESSABLE_ENTITY,"https://www.rfc-editor.org/rfc/rfc9110.html#name-422-unprocessable-content",
			HttpStatus.INTERNAL_SERVER_ERROR, "https://www.rfc-editor.org/rfc/rfc9110.html#name-500-internal-server-error",
			HttpStatus.OK, "https://www.rfc-editor.org/rfc/rfc9110.html#name-200-ok",
			HttpStatus.UNAUTHORIZED, "https://www.rfc-editor.org/rfc/rfc9110.html#name-401-unauthorized",
			HttpStatus.FORBIDDEN, "https://www.rfc-editor.org/rfc/rfc9110.html#name-403-forbidden",
			HttpStatus.NOT_ACCEPTABLE, "https://www.rfc-editor.org/rfc/rfc9110.html#name-406-not-acceptable"
		);
	
	private Logger logger = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);
	
	public static Problem buildProblem(HttpStatus status, String detail) {
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


	@ResponseStatus(HttpStatus.CONFLICT)
	@ExceptionHandler(ConflictException.class)
	public Problem handleConstraintViolation(ConflictException ex) {
		return buildProblem(HttpStatus.CONFLICT, ex.getLocalizedMessage());
	}
	
	
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(ResourceNotFoundException.class)
	public Problem handleConstraintViolation(ResourceNotFoundException ex) {
		return buildProblem(HttpStatus.NOT_FOUND, ex.getLocalizedMessage());
	}
	
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler({BadRequestException.class, MethodArgumentTypeMismatchException.class})
	public Problem handleConstraintViolation(RuntimeException ex) {		
		return buildProblem(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
	}

	
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	@ExceptionHandler(SemanticValidationException.class)
	public Problem handleConstraintViolation(SemanticValidationException ex) {
		return buildProblem(HttpStatus.UNPROCESSABLE_ENTITY, ex.getLocalizedMessage());
	}
	
	
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(NotAuthorizedException.class)
	public Problem handleConstraintViolation(NotAuthorizedException ex) {
		return buildProblem(HttpStatus.UNAUTHORIZED, ex.getLocalizedMessage());
	}
	
	
	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ExceptionHandler(ForbiddenException.class)
	public Problem handleConstraintViolation(ForbiddenException ex) {
		return buildProblem(HttpStatus.FORBIDDEN, ex.getLocalizedMessage());
	}
	
	
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({InternalException.class})
	public final Problem handleAllInternalExceptions(InternalException ex, WebRequest request) {
		return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, "La richiesta non può essere soddisfatta al momento.");
	}
	
	
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({RuntimeException.class})
	public final Problem catchAll(RuntimeException ex, WebRequest request) {
		logger.warn("Handling Uncaught Runtime Exception: {}", ex);
		return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, "La richiesta non può essere soddisfatta al momento.");
	}
	
	
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({Exception.class})
	public final Problem catchAll(Exception ex, WebRequest request) {
		logger.warn("Handling Uncaught Exception: {}", ex);
		return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, "La richiesta non può essere soddisfatta al momento.");
	}
	

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			org.springframework.http.HttpHeaders headers, HttpStatus status, WebRequest request) {

		var error = ex.getBindingResult().getAllErrors().get(0);

		return new ResponseEntity<>(
				buildProblem(HttpStatus.BAD_REQUEST, RequestUtils.extractValidationError(error)),
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
				buildProblem(HttpStatus.BAD_REQUEST,msg),
				HttpStatus.BAD_REQUEST);
	}

	
	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(
			NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

		return new ResponseEntity<>(
				buildProblem(HttpStatus.NOT_FOUND,ex.getLocalizedMessage()),
				HttpStatus.NOT_FOUND);	
		}
	
	
	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
			HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

		return new ResponseEntity<>(
				buildProblem(HttpStatus.BAD_REQUEST,ex.getLocalizedMessage()),
				HttpStatus.BAD_REQUEST);	
		}

	
	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(
			MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

		return new ResponseEntity<>(
				buildProblem(HttpStatus.BAD_REQUEST,ex.getLocalizedMessage()),
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
				buildProblem(HttpStatus.INTERNAL_SERVER_ERROR,ex.getLocalizedMessage()),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}
	

}
