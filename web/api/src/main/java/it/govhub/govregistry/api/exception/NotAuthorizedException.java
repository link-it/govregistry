package it.govhub.govregistry.api.exception;

public class NotAuthorizedException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public NotAuthorizedException() {
		this("Principal not authorized for the operation.");
	}
	

	public NotAuthorizedException(String msg) {
		super(msg);
	}

}
