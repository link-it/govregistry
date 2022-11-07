package it.govhub.rest.backoffice.exception;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;

import it.govhub.rest.backoffice.beans.Problem;
import it.govhub.rest.backoffice.utils.RequestUtils;


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
	
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler({BadRequestException.class, MethodArgumentTypeMismatchException.class})
	public Problem handleConstraintViolation(RuntimeException ex) {		
		return buildProblem(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
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
