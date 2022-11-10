package it.govhub.rest.backoffice.exception;

/**
 * Eccezione sollevata quando una certa riga di codice non dovrebbe essere mai raggiunta. 
 *
 */
public class UnreachableException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnreachableException(String msg) {
		super(msg);
	}

	public UnreachableException(Throwable t) {
		super(t);
	}

}
