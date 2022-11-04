package it.govhub.rest.backoffice.exception;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

	@ResponseStatus(HttpStatus.CONFLICT)
	@ExceptionHandler(ConflictException.class)
	public Problem handleConstraintViolation(ConflictException ex) {
		try {
			Problem ret = new Problem();
			ret.setStatus(HttpStatus.CONFLICT.value());
			ret.setTitle(HttpStatus.CONFLICT.getReasonPhrase());
			ret.setType(new URI("https://www.rfc-editor.org/rfc/rfc9110.html#name-409-conflict"));
			ret.setDetail(ex.getLocalizedMessage());
			return ret;
			
		} catch (URISyntaxException e){
			// Non deve mai fallire la new URI di sopra
			throw new RuntimeException(e);
		}
	}
	
	
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(ResourceNotFoundException.class)
	public Problem handleConstraintViolation(ResourceNotFoundException ex) {
		try {
			Problem ret = new Problem();
			ret.setStatus(HttpStatus.NOT_FOUND.value());
			ret.setTitle(HttpStatus.NOT_FOUND.getReasonPhrase());
			ret.setType(new URI("https://www.rfc-editor.org/rfc/rfc9110.html#name-404-not-found"));
			ret.setDetail(ex.getLocalizedMessage());
			return ret;
			
		} catch (URISyntaxException e){
			// Non deve mai fallire la new URI di sopra
			throw new RuntimeException(e);
		}
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



	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			org.springframework.http.HttpHeaders headers, HttpStatus status, WebRequest request) {
		ProblemModel problem = null;
		try {
			problem = ProblemModel.builder()
					.status(HttpStatus.BAD_REQUEST.value())
					.title(HttpStatus.BAD_REQUEST.getReasonPhrase())
					.type(new URI("https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request"))
					.detail(ex.getBindingResult().getAllErrors().get(0).getDefaultMessage())
					.build();
		} catch (URISyntaxException e) {
			problem = ProblemModel.builder()
					.status(HttpStatus.BAD_REQUEST.value())
					.title(HttpStatus.BAD_REQUEST.getReasonPhrase())
					.detail(ex.getMessage())
					.build();
		}
		return new ResponseEntity<>(problem, HttpStatus.BAD_REQUEST);
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
