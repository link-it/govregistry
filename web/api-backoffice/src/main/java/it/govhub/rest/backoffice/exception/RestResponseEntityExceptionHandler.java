package it.govhub.rest.backoffice.exception;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import it.govhub.rest.backoffice.beans.Problem;


@ControllerAdvice
@ResponseBody
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	
	private static Map<HttpStatus, String> problemTypes = Map.of(
			HttpStatus.CONFLICT,  "https://www.rfc-editor.org/rfc/rfc9110.html#name-409-conflict",
			HttpStatus.NOT_FOUND, "https://www.rfc-editor.org/rfc/rfc9110.html#name-404-not-found",
			HttpStatus.BAD_REQUEST,"https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request" 
		);
	
	private Problem buildProblem(HttpStatus status, String detail) {
		try {
			Problem ret = new Problem();
			ret.setStatus(status.value());
			ret.setTitle(status.getReasonPhrase());
			ret.setType(new URI(problemTypes.get(status)));
			ret.setDetail(detail);
			return ret;
			
		} catch (URISyntaxException e){
			// Non deve mai fallire la new URI di sopra
			throw new RuntimeException(e);
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

	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			org.springframework.http.HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		return new ResponseEntity<>(
				buildProblem(HttpStatus.BAD_REQUEST, extractValidationError(ex)),
				HttpStatus.BAD_REQUEST);
	}
	
	/**
	 * Crea un messaggio che descrive un errore di validazione.
	 * Più leggibile per una API rispetto a quello restituito di default.
	 * 	
	 */
	private static String extractValidationError(MethodArgumentNotValidException ex) {
		var error = ex.getBindingResult().getAllErrors().get(0);
		if (error instanceof FieldError) {			
			var ferror = (FieldError) error;
			
			return "Field error in object '" + error.getObjectName() + "' on field '" + ferror.getField() +
					"': rejected value [" + ObjectUtils.nullSafeToString(ferror.getRejectedValue()) + "]; " +
					error.getDefaultMessage();
		}
		return error.toString();		
	}
	
	/*@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	@ExceptionHandler(SemanticValidationException.class)
	public ProblemModel handleConstraintViolation(SemanticValidationException ex) {
		try {
			return ProblemModel.builder()
					.status(HttpStatus.UNPROCESSABLE_ENTITY.value())
					.title(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase())
					.type(new URI("https://www.rfc-editor.org/rfc/rfc9110.html#name-422-unprocessable-content"))
					.detail(ex.getLocalizedMessage())
					.build();
		} catch (URISyntaxException e) {
			return ProblemModel.builder()
					.status(HttpStatus.UNPROCESSABLE_ENTITY.value())
					.title(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase())
					.detail(ex.getLocalizedMessage())
					.build();
		}

	}




	
	@ResponseStatus(HttpStatus.BAD_GATEWAY)
	@ExceptionHandler(BadGatewayException.class)
	public ProblemModel handleBadGatewayException(BadGatewayException ex) {
		return ProblemModel.builder()
				.status(HttpStatus.BAD_GATEWAY.value())
				.title(HttpStatus.BAD_GATEWAY.getReasonPhrase())
				.detail(ex.getMessage())
				.build();
	}
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(javax.validation.ConstraintViolationException.class)
	public ProblemModel handleConstraintViolation(javax.validation.ConstraintViolationException ex) {
		return ProblemModel.builder()
				.status(HttpStatus.BAD_REQUEST.value())
				.title(HttpStatus.BAD_REQUEST.getReasonPhrase())
				.detail(ex.getMessage())
				.build();
	}*/
}
