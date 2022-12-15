package it.govhub.govregistry.commons.exception;

public class SemanticValidationException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public SemanticValidationException(String message) {
		super(message);
	}

}
