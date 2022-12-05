package it.govhub.govregistry.api.exception;

public class SemanticValidationException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public SemanticValidationException(String message) {
		super(message);
	}

}
