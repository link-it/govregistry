package it.govhub.rest.backoffice.exception;

public class BadRequestException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public BadRequestException(String message) {
		super(message);
	}

	public BadRequestException(Throwable t) {
		super(t);
	}

}